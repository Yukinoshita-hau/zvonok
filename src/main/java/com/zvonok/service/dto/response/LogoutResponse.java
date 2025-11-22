package com.zvonok.service.dto.response;

import lombok.Value;

@Value
public class LogoutResponse {

    String message;
    boolean allDevices;
}

