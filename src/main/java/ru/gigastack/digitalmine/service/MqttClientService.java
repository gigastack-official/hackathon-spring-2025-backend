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
import ru.gigastack.digitalmine.dto.RFIDDto;
import ru.gigastack.digitalmine.dto.SensorDataDto;
import ru.gigastack.digitalmine.model.CardUser;
import ru.gigastack.digitalmine.model.RfidLog;
import ru.gigastack.digitalmine.model.SensorData;
import ru.gigastack.digitalmine.repository.RfidLogRepository;
import ru.gigastack.digitalmine.repository.SensorDataRepository;
import ru.gigastack.digitalmine.service.CardUserService;
import ru.gigastack.digitalmine.service.LightingService;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MqttClientService implements MqttCallback {

    private static final Logger logger = LoggerFactory.getLogger(MqttClientService.class);

    private static final String BROKER_URL = "tcp://emqx.gigafs.v6.navy:1883";
    private static final String MQTT_CLIENT_ID = "DigitalMineBackendClient";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin76767676";

    // --- Старый топик для сенсоров ---
    private static final String SENSORS_TOPIC = "sensors/test";

    // --- Новый топик для RFID ---
    private static final String RFID_TOPIC = "rfid/scans";
    private static final String RFID_RESP_TOPIC = "rfid/responses";

    // Для уведомления о газовой тревоге
    private static final String ALERT_TOPIC = "alert/updates";
    private static final String LIGHTING_TOPIC = "lighting/control";

    // Порог газа
    @Value("${sensor.gas.threshold:50.0}")
    private double gasThreshold;

    private MqttClient client;

    private final SimpMessagingTemplate messagingTemplate;
    private final SensorDataRepository sensorDataRepository;
    private final RfidLogRepository rfidLogRepository;
    private final ObjectMapper objectMapper;
    private final LightingService lightingService;
    private final CardUserService cardUserService;

    // Показывает, активно ли сейчас «угроза» загазованности
    private boolean threatActive = false;

    public MqttClientService(
            SimpMessagingTemplate messagingTemplate,
            SensorDataRepository sensorDataRepository,
            RfidLogRepository rfidLogRepository,
            ObjectMapper objectMapper,
            LightingService lightingService,
            CardUserService cardUserService
    ) {
        this.messagingTemplate = messagingTemplate;
        this.sensorDataRepository = sensorDataRepository;
        this.rfidLogRepository = rfidLogRepository;
        this.objectMapper = objectMapper;
        this.lightingService = lightingService;
        this.cardUserService = cardUserService;
    }

    @PostConstruct
    public void init() {
        try {
            client = new MqttClient(BROKER_URL, MQTT_CLIENT_ID);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());
            options.setCleanSession(true);
            options.setAutomaticReconnect(true); // Чтоб заново подключался при разрыве

            client.setCallback(this);
            client.connect(options);
            logger.info("Connected to MQTT broker at {}", BROKER_URL);

            // Подписываемся и на сенсорный топик, и на RFID
            client.subscribe(SENSORS_TOPIC);
            client.subscribe(RFID_TOPIC);

            logger.info("Subscribed to MQTT topics: {}, {}", SENSORS_TOPIC, RFID_TOPIC);

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

        // Для наглядности шлём входящий payload на WebSocket
        messagingTemplate.convertAndSend("/topic/sensorData", payload);

        // Обрабатываем
        if (SENSORS_TOPIC.equals(topic)) {
            handleSensorData(payload);
        } else if (RFID_TOPIC.equals(topic)) {
            handleRfidScan(payload);
        } else {
            logger.warn("Unexpected topic: {}", topic);
        }
    }

    // ---------------- ЛОГИКА СЕНСОРОВ (Старая) ----------------------
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

            // Проверка на превышение
            double gasLevel = sensorData.getGasLevel();
            if (gasLevel > gasThreshold) {
                // Угроза!
                if (!threatActive) {
                    threatActive = true;
                    onThreatStarted();
                }
            } else {
                // газа меньше порога -> если была угроза, завершаем
                if (threatActive) {
                    threatActive = false;
                    onThreatEnded();
                }
            }

        } catch (Exception e) {
            logger.error("Error parsing or saving sensor data from MQTT payload", e);
        }
    }

    private void onThreatStarted() {
        logger.warn("THREAT DETECTED: Gas level is above threshold.");
        lightingService.overrideLighting("#FF0000", 100);

        // Публикуем событие "THREAT_STARTED"
        publish(ALERT_TOPIC, "{\"status\":\"THREAT_STARTED\"}");

        // Жёстко указываем освещение #FF0000
        publishLightingControlOverride(true, "#FF0000", 100);
    }

    private void onThreatEnded() {
        logger.info("THREAT ENDED: Gas level is below threshold again.");
        lightingService.restoreUserSettings();

        publish(ALERT_TOPIC, "{\"status\":\"THREAT_ENDED\"}");

        // Возвращаем на текущие настройки
        publishLightingControlOverride(
                lightingService.getCurrentSettings().getPower(),
                lightingService.getCurrentSettings().getColor(),
                lightingService.getCurrentSettings().getBrightness()
        );
    }

    private void publishLightingControlOverride(Boolean power, String color, Integer brightness) {
        try {
            LightingControlDto dto = new LightingControlDto();
            dto.setPower(power);
            dto.setColor(color);
            dto.setBrightness(brightness);

            String json = objectMapper.writeValueAsString(dto);
            publish(LIGHTING_TOPIC, json);

        } catch (Exception e) {
            logger.error("Failed to serialize LightingControlDto to JSON", e);
        }
    }

    // -------------- НОВАЯ ЛОГИКА ДЛЯ RFID-SCANS ---------------------


    private void handleRfidScan(String payload) {
        try {
            // Пришло что-то вроде: { "tagId": "123" }
            RFIDDto rfidDto = objectMapper.readValue(payload, RFIDDto.class);

            if (rfidDto.getTagId() == null || rfidDto.getTagId().isBlank()) {
                logger.warn("RFID MQTT message has no tagId, skipping");
                return;
            }

            // Определяем action (enter/exit) — например, смотрим, была ли последняя запись enter
            String newAction = resolveAction(rfidDto.getTagId());

            // Проверяем пользователя в card_users
            Optional<CardUser> userOpt = cardUserService.findByCardId(rfidDto.getTagId());
            String status = userOpt.isPresent() ? "allowed" : "denied";
            String fullName = userOpt.map(CardUser::getFullName).orElse(null);

            // Сохраняем в rfid_log
            RfidLog log = new RfidLog();
            log.setTagId(rfidDto.getTagId());
            log.setAction(newAction);
            log.setStatus(status);
            log.setFullName(fullName);
            log.setTimestamp(LocalDateTime.now());
            rfidLogRepository.save(log);

            logger.info("Saved RFID log: tagId={}, action={}, status={}, fullName={}",
                    rfidDto.getTagId(), newAction, status, fullName);

            // Формируем ответ
            RFIDDto response = new RFIDDto();
            response.setTagId(rfidDto.getTagId());
            response.setAction(newAction);
            response.setStatus(status);
            // Если нужно вернуть имя, добавьте поле в DTO

            String responsePayload = objectMapper.writeValueAsString(response);

            // Публикуем ответ уже НЕ в rfid/scans, а в rfid/responses
            publish(RFID_RESP_TOPIC, responsePayload);

        } catch (Exception e) {
            logger.error("Error handling RFID scan from MQTT payload", e);
        }
    }

    /**
     * Смотрим, была ли последняя запись "enter" или "exit". Если не было записей или была "exit" — ставим "enter".
     * Если была "enter" — ставим "exit".
     */
    private String resolveAction(String tagId) {
        Optional<RfidLog> lastLog = rfidLogRepository.findTopByTagIdOrderByTimestampDesc(tagId);
        if (lastLog.isEmpty()) {
            // Первая запись: "enter"
            return "enter";
        }
        String lastAction = lastLog.get().getAction();
        return "enter".equalsIgnoreCase(lastAction) ? "exit" : "enter";
    }

    // ---------------------------------------------------------------

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        logger.info("MQTT message delivery complete");
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                logger.info("Disconnected from MQTT broker");
            }
        } catch (MqttException e) {
            logger.error("Error disconnecting MQTT client", e);
        }
    }

    /**
     * Метод для публикации сообщений в MQTT (не меняем логику).
     */
    public void publish(String topic, String payload) {
        try {
            if (client == null || !client.isConnected()) {
                logger.warn("MQTT client is not connected, cannot publish: {}", payload);
                return;
            }
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            client.publish(topic, message);
            logger.info("Published MQTT message to topic {}: {}", topic, payload);
        } catch (MqttException e) {
            logger.error("Error publishing MQTT message", e);
        }
    }
}