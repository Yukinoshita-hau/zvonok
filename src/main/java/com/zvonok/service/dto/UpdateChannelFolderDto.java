package com.zvonok.service.dto;

import lombok.Data;

@Data
public class UpdateChannelFolderDto {
    private String name;
    private Integer position;
    private Boolean collapsed;
    private Boolean active;
}

