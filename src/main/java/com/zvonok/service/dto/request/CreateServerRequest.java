package com.zvonok.service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateServerRequest {

    @NotBlank(message = "Server name cannot be empty")
    @Size(max = 100, message = "Server name cannot exceed 100 characters")
    private String name;

    @Min(value = 10, message = "Minimum number of participants: 10")
    @Max(value = 10000, message = "Maximum number of participants: 10000")
    private Integer maxMembers;
}
