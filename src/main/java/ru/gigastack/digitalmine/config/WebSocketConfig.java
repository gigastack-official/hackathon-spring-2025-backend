package ru.gigastack.digitalmine.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Настройка брокера сообщений: мы используем простой in-memory брокер для сообщений,
    // которые будут отправляться клиентам (например, на топики /topic или /queue).
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        // Префикс, с которого начинаются все сообщения, отправляемые с клиента на сервер.
        config.setApplicationDestinationPrefixes("/app");
    }

    // Регистрация конечной точки, через которую клиенты будут подключаться к WebSocket.
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // Разрешаем подключение с любых доменов (при необходимости ограничьте список)
                .setAllowedOriginPatterns("*")
                // Поддержка SockJS для обеспечения кроссбраузерности
                .withSockJS();
    }
}