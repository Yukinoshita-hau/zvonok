package com.zvonok.service.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateRoomDto {
    @Size(max = 100, message = "Room name must not exceed 100 characters")
    private String name;
}

