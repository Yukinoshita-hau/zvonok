package com.zvonok.service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutRequest {

    private String refreshToken;

    private boolean allDevices = false;

    public boolean hasRefreshToken() {
        return refreshToken != null && !refreshToken.isBlank();
    }
}

