package com.example_project_name.websocket;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

// STOMP over WebSocket (SockJS fallback). The HTTP handshake is open (see SpringSecurityConfig);
// clients must send "Authorization: Bearer <jwt>" as a STOMP CONNECT header, which is validated with
// the same JwtDecoder/issuer list as the REST API.
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    public static final String WEBSOCKETS_ENDPOINT = "/websockets";
    public static final String TOPIC_PREFIX = "/topic";
    public static final String TOPIC_MY_EVENT = "/my_events";

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

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

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String header = accessor.getFirstNativeHeader("Authorization");
                    if (header == null || !header.startsWith("Bearer ")) {
                        throw new BadCredentialsException("Missing bearer token on STOMP CONNECT");
                    }
                    accessor.setUser(jwtAuthenticationConverter.convert(jwtDecoder.decode(header.substring(7))));
                }
                return message;
            }
        });
    }
}
