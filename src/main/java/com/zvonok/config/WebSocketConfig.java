package com.zvonok.config;

import com.zvonok.handler.JwtHandshakeHandler;
import com.zvonok.handler.JwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final JwtHandshakeHandler jwtHandshakeHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/app");
        config.enableSimpleBroker("/topic", "/queue");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // С SockJS (для браузеров)
        registry.addEndpoint("/ws")
                .setHandshakeHandler(jwtHandshakeHandler)
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOrigins("*")
                .withSockJS();
    
        // БЕЗ SockJS (для Postman)
        registry.addEndpoint("/ws-raw")
                .setHandshakeHandler(jwtHandshakeHandler)
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOrigins("*");

    } 
}
