package com.zvonok.service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ServerBanResponse {
    private Long userId;
    private String username;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Long bannedById;
    private String bannedByUsername;
}

