package com.zvonok.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendFriendRequestRequest {

    @NotNull(message = "Receiver id must be provided")
    private Long receiverId;
}

