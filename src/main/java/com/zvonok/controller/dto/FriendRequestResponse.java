package com.zvonok.controller.dto;

import com.zvonok.model.enumeration.FriendRequestStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class FriendRequestResponse {
    Long requestId;
    Long senderId;
    String senderUsername;
    Long receiverId;
    String receiverUsername;
    FriendRequestStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

