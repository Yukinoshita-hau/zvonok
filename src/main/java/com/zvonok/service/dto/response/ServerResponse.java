package com.zvonok.service.dto.response;

import com.zvonok.model.ChannelFolder;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ServerResponse {
    private Long id;
    private String name;
    private String inviteCode;
    private Integer maxMembers;
    private Long memberCount;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
    private List<ChannelFolder> channelFolders;
}