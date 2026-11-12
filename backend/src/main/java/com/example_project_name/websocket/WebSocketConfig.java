package com.example_project_name.websocket;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// STOMP over WebSocket (SockJS fallback). The HTTP handshake goes through the normal security chain, so the
// browser authenticates it with Negotiate like any other request and Spring attaches that user to the session.
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    public static final String WEBSOCKETS_ENDPOINT = "/websockets";
    public static final String TOPIC_PREFIX = "/topic";
    public static final String TOPIC_MY_EVENT = "/my_events";

    @Value("${app.websocket.allowed-origin-patterns:http://localhost:*}")
    private List<String> allowedOriginPatterns;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker(TOPIC_PREFIX);
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(WEBSOCKETS_ENDPOINT)
            .setAllowedOriginPatterns(allowedOriginPatterns.toArray(String[]::new))
            .withSockJS();
    }
}
