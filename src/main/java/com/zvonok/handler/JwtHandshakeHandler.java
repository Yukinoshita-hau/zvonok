package com.zvonok.handler;

import com.zvonok.security.JwtTokenProvider;
import com.zvonok.security.dto.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(
            @Nullable ServerHttpRequest request,
            @Nullable WebSocketHandler wsHandler,
            @Nullable Map<String, Object> attributes
    ) {

        if (attributes == null) {
            log.warn("WebSocket handshake: attributes is null");
            return null;
        }

        String username = (String) attributes.get("username");
        String token = (String) attributes.get("token");

        if (username == null || token == null) {
            log.warn("WebSocket handshake: missing username or token in attributes");
            return null;
        }

        log.info("WebSocket handshake: creating UserPrincipal for user {}", username);

        return new UserPrincipal(username, token);
    }
}
