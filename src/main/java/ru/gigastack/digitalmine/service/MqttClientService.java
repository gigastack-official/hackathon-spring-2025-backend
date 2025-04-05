package ru.gigastack.digitalmine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.gigastack.digitalmine.dto.LightingControlDto;
import ru.gigastack.digitalmine.dto.SensorDataDto;
import ru.gigastack.digitalmine.model.SensorData;
import ru.gigastack.digitalmine.repository.SensorDataRepository;

import java.time.LocalDateTime;

/**
 * Пример доработанного MqttClientService, который:
 * 1) Парсит входящие сообщения с датчиков (JSON вида SensorDataDto).
 * 2) Сохраняет результаты в БД sensor_data.
 * 3) Детектирует опасное превышение уровня газа и включает аварийное освещение.
 * 4) После нормализации уровня газа восстанавливает освещение.
 * 5) Отправляет события о начале/окончании угрозы в отдельный топик (например, "alert/updates"),
 *    а также в "lighting/control" для реального управления лентой.
 */
@Service
public class MqttClientService implements MqttCallback {

    private static final Logger logger = LoggerFactory.getLogger(MqttClientService.class);

    private static final String BROKER_URL = "tcp://emqx.gigafs.v6.navy:1883";
    private static final String MQTT_CLIENT_ID = "DigitalMineBackendClient";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin76767676";

    // Топики, которые мы будем слушать
    private static final String SENSORS_TOPIC = "sensors/test";

    // Топики, на которые мы будем публиковать в случае тревоги / нормализации:
    private static final String ALERT_TOPIC = "alert/updates";        // например, для фронта
    private static final String LIGHTING_TOPIC = "lighting/control";  // для реального управления лентой

    @Value("${sensor.gas.threshold:50.0}")
    private double gasThreshold;

    private MqttClient client;

    private final SimpMessagingTemplate messagingTemplate;
    private final SensorDataRepository sensorDataRepository;
    private final ObjectMapper objectMapper;
    private final LightingService lightingService;

    // Будем хранить состояние угрозы, чтобы не посылать повторные сообщения при каждом новом пакете,
    // если ничего не поменялось.
    private boolean threatActive = false;

    public MqttClientService(
            SimpMessagingTemplate messagingTemplate,
            SensorDataRepository sensorDataRepository,
            ObjectMapper objectMapper,
            LightingService lightingService
    ) {
        this.messagingTemplate = messagingTemplate;
        this.sensorDataRepository = sensorDataRepository;
        this.objectMapper = objectMapper;
        this.lightingService = lightingService;
    }

    @PostConstruct
    public void init() {
        try {
            client = new MqttClient(BROKER_URL, MQTT_CLIENT_ID);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());
            options.setCleanSession(true);

            client.setCallback(this);
            client.connect(options);
            logger.info("Connected to MQTT broker at {}", BROKER_URL);

            // Подписываемся на топик с данными датчиков
            client.subscribe(SENSORS_TOPIC);
            logger.info("Subscribed to MQTT topic: {}", SENSORS_TOPIC);

        } catch (MqttException e) {
            logger.error("Error initializing MQTT client", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        logger.error("MQTT connection lost", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        logger.info("Received MQTT message on topic {}: {}", topic, payload);

        // Передаём "сырой" payload во WebSocket (если фронт подписан на /topic/sensorData)
        messagingTemplate.convertAndSend("/topic/sensorData", payload);

        // Обрабатываем только если это топик для датчиков
        if (SENSORS_TOPIC.equals(topic)) {
            handleSensorData(payload);
        }
    }

    private void handleSensorData(String payload) {
        try {
            SensorDataDto sensorDataDto = objectMapper.readValue(payload, SensorDataDto.class);

            // Сохраняем в БД
            SensorData sensorData = new SensorData();
            sensorData.setGasLevel(sensorDataDto.getGasLevel());
            sensorData.setTemperature(sensorDataDto.getTemperature());
            sensorData.setHumidity(sensorDataDto.getHumidity());
            sensorData.setTimestamp(LocalDateTime.now());

            sensorDataRepository.save(sensorData);
            logger.info("Sensor data saved: {}", sensorData);

            // --- Детектируем превышение газа ---
            double gasLevel = sensorData.getGasLevel();
            if (gasLevel > gasThreshold) {
                // Угроза!
                if (!threatActive) {
                    threatActive = true;
                    onThreatStarted();
                }
            } else {
                // газа меньше порога -> при условии, что до этого была угроза, завершаем
                if (threatActive) {
                    threatActive = false;
                    onThreatEnded();
                }
            }

        } catch (Exception e) {
            logger.error("Error parsing or saving sensor data from MQTT payload", e);
        }
    }

    /**
     * Вызывается, когда уровень газа стал выше порога и угроза ещё не зафиксирована.
     */
    private void onThreatStarted() {
        logger.warn("THREAT DETECTED: Gas level is above threshold.");

        // 1) override освещение (красный)
        lightingService.overrideLighting("#FF0000", 100);

        // 2) Отправляем уведомление на фронт (websocket /topic/alert или /topic/updates).
        //    Но поскольку брокер MQTT и фронт может слушать в MQTT, публикуем и туда, и через WebSocket, как нужно.
        publish(ALERT_TOPIC, "THREAT_STARTED");

        // 3) Отправляем на lighting/control, что надо принудительно задать #FF0000
        LightingControlDto overrideDto = new LightingControlDto();
        overrideDto.setPower(true);
        overrideDto.setColor("#FF0000");
        overrideDto.setBrightness(100);

        publishLightingControlOverride(overrideDto);
    }

    /**
     * Вызывается, когда уровень газа снова стал ниже порога (конец угрозы).
     */
    private void onThreatEnded() {
        logger.info("THREAT ENDED: Gas level is below threshold again.");

        // 1) восстанавливаем пользовательские настройки
        lightingService.restoreUserSettings();

        // 2) публикация для фронта
        publish(ALERT_TOPIC, "THREAT_ENDED");

        // 3) публикация на lighting/control, что нужно вернуть прежний цвет
        //    (получим из lightingService.getCurrentSettings())
        LightingControlDto restored = lightingService.getCurrentSettings();
        publishLightingControlOverride(restored);
    }

    /**
     * Упрощённая публикация "lighting/control" в JSON формате.
     */
    private void publishLightingControlOverride(LightingControlDto dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            publish(LIGHTING_TOPIC, json);
        } catch (Exception e) {
            logger.error("Failed to serialize LightingControlDto to JSON", e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        logger.info("MQTT message delivery complete");
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (client != null) {
                client.disconnect();
                logger.info("Disconnected from MQTT broker");
            }
        } catch (MqttException e) {
            logger.error("Error disconnecting MQTT client", e);
        }
    }

    /**
     * Метод для публикации сообщений в MQTT.
     */
    public void publish(String topic, String payload) {
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            client.publish(topic, message);
            logger.info("Published MQTT message to topic {}: {}", topic, payload);
        } catch (MqttException e) {
            logger.error("Error publishing MQTT message", e);
        }
    }
}