package com.zvonok.controller.dto;

import com.zvonok.model.enumeration.MessageType;
import com.zvonok.service.dto.EventType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChannelMessageResponse {
    private Long id;
    private String content;
    private String senderUsername;
    private Long senderId;
    private LocalDateTime sentAt;
    private MessageType messageType;
    private Long channelId;
    private String channelName;
    private Long serverId;
    private EventType eventType;
    private String replyToMessageId;
    private Boolean isEdited;
}
