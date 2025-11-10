package com.zvonok.service.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateServerRoleRequest {

    @Size(max = 50)
    private String name;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color must be a hex value like #FFFFFF")
    private String color;

    private Integer position;

    private Long serverPermissions;

    private Boolean mentionable;

    private Boolean active;
}

