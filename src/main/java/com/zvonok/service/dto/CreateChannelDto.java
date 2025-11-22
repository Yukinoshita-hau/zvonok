package com.zvonok.service.dto;

import com.zvonok.model.enumeration.ChannelType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateChannelDto {
    private String name;
    private Long folderId;
    private ChannelType type;
    private Integer position;
    private Integer userLimit = 100;
    private String topic;
}
