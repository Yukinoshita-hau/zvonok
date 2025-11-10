package com.zvonok.service.dto;

import lombok.Data;

@Data
public class UpdateServerRoleDto {
    private String name;
    private String color;
    private Integer position;
    private Long serverPermissions;
    private Boolean mentionable;
    private Boolean active;
}

