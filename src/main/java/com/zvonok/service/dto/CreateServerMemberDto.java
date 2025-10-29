package com.zvonok.service.dto;

import com.zvonok.model.Server;
import com.zvonok.model.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateServerMemberDto {

    private User user;
    private Server server;
    private Permission PersonalPermission;
}
