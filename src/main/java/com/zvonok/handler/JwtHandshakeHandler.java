package com.zvonok.handler;

import com.zvonok.security.dto.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(
            @Nullable ServerHttpRequest request,
            @Nullable WebSocketHandler wsHandler,
            @Nullable Map<String, Object> attributes
    ) {

        if (attributes == null) {
            return null;
        }

        String username = (String) attributes.get("username");
        String token = (String) attributes.get("token");

        if (username == null || token == null) {
            return null;
        }

        return new UserPrincipal(username, token);
    }
}
