package com.zvonok.controller.dto;

import com.zvonok.model.enumeration.UserStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class FriendResponse {
    Long friendshipId;
    Long friendId;
    String friendUsername;
    String friendEmail;
    String friendAvatarUrl;
    UserStatus friendStatus;
    LocalDateTime friendshipSince;
}

