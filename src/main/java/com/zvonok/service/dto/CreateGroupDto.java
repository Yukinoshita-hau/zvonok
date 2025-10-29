package com.zvonok.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateGroupDto {

    private String roomName;

    private List<String> roomMemberUsernames;
}
