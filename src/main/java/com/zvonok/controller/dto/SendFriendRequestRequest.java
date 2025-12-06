package com.zvonok.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendFriendRequestRequest {

    @NotBlank(message = "Receiver username must be provided")
    private String receiverUsername;
}

