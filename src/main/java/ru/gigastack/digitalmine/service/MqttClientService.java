package ru.gigastack.digitalmine.service;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class MqttClientService {

    private static final Logger logger = LoggerFactory.getLogger(MqttClientService.class);

    // Подключаемся по TCP. Если требуется WebSocket, можно использовать URL вида:
    // "ws://emqx.gigafs.v6.navy:8083/mqtt"
    private static final String BROKER_URL = "tcp://emqx.gigafs.v6.navy:1883";
    private static final String MQTT_CLIENT_ID = "DigitalMineBackendClient";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin76767676";

    private MqttClient client;

    @PostConstruct
    public void init() {
        try {
            // Создаём экземпляр клиента
            client = new MqttClient(BROKER_URL, MQTT_CLIENT_ID);

            // Настройки подключения: указываем имя пользователя и пароль, чистую сессию
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());
            options.setCleanSession(true);

            // Устанавливаем обратный вызов для обработки событий (сообщения, потеря соединения, завершение доставки)
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    logger.error("MQTT connection lost", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());
                    logger.info("Received message on topic {}: {}", topic, payload);
                    // Здесь можно добавить логику для пересылки сообщения через WebSocket или его обработки
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    logger.info("MQTT message delivery complete");
                }
            });

            // Подключаемся к брокеру
            client.connect(options);
            logger.info("Connected to MQTT broker at {}", BROKER_URL);

            // Подписываемся на топик. Здесь можно добавить подписки на необходимые топики.
            client.subscribe("sensors/test");
            logger.info("Subscribed to topic sensors/test");

        } catch (MqttException e) {
            logger.error("Error initializing MQTT client", e);
        }
    }

    // Метод публикации сообщений на заданный топик
    public void publish(String topic, String payload) {
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1); // Устанавливаем QoS по необходимости
            client.publish(topic, message);
            logger.info("Published message to topic {}: {}", topic, payload);
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