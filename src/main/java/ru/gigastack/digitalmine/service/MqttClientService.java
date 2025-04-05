package ru.gigastack.digitalmine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.gigastack.digitalmine.dto.SensorDataDto;
import ru.gigastack.digitalmine.model.SensorData;
import ru.gigastack.digitalmine.repository.SensorDataRepository;

import java.time.LocalDateTime;

@Service
public class MqttClientService implements MqttCallback {

    private static final Logger logger = LoggerFactory.getLogger(MqttClientService.class);

    // URL, логин и пароль у вас уже прописаны
    private static final String BROKER_URL = "tcp://emqx.gigafs.v6.navy:1883";
    private static final String MQTT_CLIENT_ID = "DigitalMineBackendClient";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin76767676";

    private MqttClient client;

    private final SimpMessagingTemplate messagingTemplate;
    private final SensorDataRepository sensorDataRepository; // <-- добавляем
    private final ObjectMapper objectMapper;                 // <-- для парсинга JSON (через Jackson)

    public MqttClientService(SimpMessagingTemplate messagingTemplate,
                             SensorDataRepository sensorDataRepository,  // <-- внедряем репозиторий
                             ObjectMapper objectMapper) {                 // <-- и ObjectMapper
        this.messagingTemplate = messagingTemplate;
        this.sensorDataRepository = sensorDataRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        try {
            client = new MqttClient(BROKER_URL, MQTT_CLIENT_ID);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());
            options.setCleanSession(true);

            // Настраиваем callbacks
            client.setCallback(this);

            // Подключаемся к брокеру
            client.connect(options);
            logger.info("Connected to MQTT broker at {}", BROKER_URL);

            // Подписываемся на один или несколько топиков
            client.subscribe("sensors/test");
            logger.info("Subscribed to MQTT topic: sensors/test");

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
        // 1) Преобразуем payload в строку
        String payload = new String(message.getPayload());
        logger.info("Received MQTT message on topic {}: {}", topic, payload);

        // 2) Если у нас JSON данных датчиков, пробуем распарсить
        try {
            // Допустим, payload приходит в том же формате, что и ваш SensorDataDto
            SensorDataDto sensorDataDto = objectMapper.readValue(payload, SensorDataDto.class);

            // 3) Создаём сущность и сохраняем в БД
            SensorData sensorData = new SensorData();
            sensorData.setGasLevel(sensorDataDto.getGasLevel());
            sensorData.setTemperature(sensorDataDto.getTemperature());
            sensorData.setHumidity(sensorDataDto.getHumidity());
            sensorData.setTimestamp(LocalDateTime.now()); // ставим текущее время или используем из DTO

            sensorDataRepository.save(sensorData);
            logger.info("Sensor data saved to DB: {}", sensorData);

        } catch (Exception e) {
            logger.error("Error parsing or saving sensor data from MQTT payload", e);
        }

        // 4) Передаём данные через WebSocket всем подписанным клиентам (по желанию)
        messagingTemplate.convertAndSend("/topic/sensorData", payload);
        logger.debug("Sent payload via WebSocket to /topic/sensorData");
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