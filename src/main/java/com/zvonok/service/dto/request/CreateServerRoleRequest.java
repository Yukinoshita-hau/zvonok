package com.zvonok.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateServerRoleRequest {

    @NotBlank
    @Size(max = 50)
    private String name;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color must be a hex value like #FFFFFF")
    private String color = "#FFFFFF";

    private Integer position = 0;

    @NotNull
    private Long serverPermissions;

    private boolean mentionable = true;
}

