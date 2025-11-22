package com.zvonok.service.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateServerMemberNicknameRequest {

    @Size(max = 32, message = "Nickname must be at most 32 characters")
    private String nickname;
}

