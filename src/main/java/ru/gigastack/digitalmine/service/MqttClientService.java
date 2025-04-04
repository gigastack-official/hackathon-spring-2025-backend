package ru.gigastack.digitalmine.service;

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

@Service
public class MqttClientService {

    private static final Logger logger = LoggerFactory.getLogger(MqttClientService.class);

    // Используем TCP; для WebSocket можно изменить URL на: "ws://emqx.gigafs.v6.navy:8083/mqtt"
    private static final String BROKER_URL = "tcp://emqx.gigafs.v6.navy:1883";
    private static final String MQTT_CLIENT_ID = "DigitalMineBackendClient";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin76767676";

    private MqttClient client;

    // Inject SimpMessagingTemplate для отправки сообщений через WebSocket
    private final SimpMessagingTemplate messagingTemplate;

    public MqttClientService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            client = new MqttClient(BROKER_URL, MQTT_CLIENT_ID);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());
            options.setCleanSession(true);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    logger.error("MQTT connection lost", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());
                    logger.info("Received MQTT message on topic {}: {}", topic, payload);

                    // Передаём данные через WebSocket всем подписанным клиентам
                    messagingTemplate.convertAndSend("/topic/sensorData", payload);
                    logger.debug("Sent payload via WebSocket to /topic/sensorData");
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    logger.info("MQTT message delivery complete");
                }
            });

            client.connect(options);
            logger.info("Connected to MQTT broker at {}", BROKER_URL);

            // Подписываемся на топик для получения данных с датчиков (расширьте список топиков при необходимости)
            client.subscribe("sensors/test");
            client.subscribe("pizda");
            logger.info("Subscribed to MQTT topic sensors/test");

        } catch (MqttException e) {
            logger.error("Error initializing MQTT client", e);
        }
    }

    /**
     * Метод для публикации сообщений в MQTT.
     *
     * @param topic   топик, в который публикуется сообщение
     * @param payload текст сообщения
     */
    public void publish(String topic, String payload) {
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1); // Используем QoS 1; можно изменить при необходимости
            client.publish(topic, message);
            logger.info("Published MQTT message to topic {}: {}", topic, payload);
        } catch (MqttException e) {
            logger.error("Error publishing MQTT message", e);
        }
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
}