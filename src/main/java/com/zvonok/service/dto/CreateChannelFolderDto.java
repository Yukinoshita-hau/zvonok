package com.zvonok.service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateChannelFolderDto {
    private String name;
    private Long serverId;
    private Integer position;
}
