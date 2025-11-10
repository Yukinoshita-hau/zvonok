package com.zvonok.service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ServerMemberResponse {
    private Long userId;
    private String username;
    private String avatarUrl;
    private String nickname;
    private LocalDateTime joinedAt;
    private List<String> roles;
    private Boolean isOwner;
}
