package com.zvonok.service.dto;

import com.zvonok.model.enumeration.ChannelType;
import lombok.Data;

@Data
public class UpdateChannelDto {
    private String name;
    private ChannelType type;
    private Integer position;
    private Integer userLimit;
    private Integer slowModeSeconds;
    private String topic;
    private Boolean nsfw;
    private Boolean active;
}

