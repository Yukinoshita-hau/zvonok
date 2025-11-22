package com.zvonok.controller.dto;

import com.zvonok.model.enumeration.MessageType;
import com.zvonok.service.dto.EventType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private long id;
    private String content;
    private String senderUsername;
    private LocalDateTime sentAt;
    private MessageType messageType;
    private Long roomId;
    private EventType eventType;
}
