package com.zvonok.service.dto.request;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class UpdateServerRequest {

    @Size(max = 100, message = "Название сервера не может превышать 100 символов")
    private String name;

    @Min(value = 10, message = "Минимальное количество участников: 10")
    @Max(value = 10000, message = "Максимальное количество участников: 10000")
    private Integer maxMembers;
}