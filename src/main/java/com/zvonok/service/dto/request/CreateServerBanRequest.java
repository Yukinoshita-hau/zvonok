package com.zvonok.service.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateServerBanRequest {

    @NotNull
    private Long targetUserId;

    @Size(max = 255)
    private String reason;

    private LocalDateTime expiresAt;
}

