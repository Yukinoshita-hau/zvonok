package com.zvonok.service;

import com.zvonok.exception.ServerMemberRoleNotFoundException;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.model.ServerMember;
import com.zvonok.model.ServerMemberRole;
import com.zvonok.model.ServerRole;
import com.zvonok.repository.ServerMemberRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
@Slf4j
public class ServerMemberRoleService {

    private final ServerMemberRoleRepository serverMemberRoleRepository;
    private final ServerMemberService serverMemberService;
    private final ServerRoleService serverRoleService;
    private final UserService userService;

    public ServerMemberRole getServerMemberRole(Long serverMemberRoleId) {
        return serverMemberRoleRepository.findById(serverMemberRoleId).orElseThrow(() -> new ServerMemberRoleNotFoundException(HttpResponseMessage.HTTP_SERVER_MEMBER_ROLE_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));
    }

    public ServerMemberRole crateServerMemberRole(Long serverMemberId, Long serverRoleId , Long assignedById) {
        ServerMember member = serverMemberService.getServerMember(serverMemberId);
        ServerRole role = serverRoleService.getServerRole(serverRoleId);

        ServerMemberRole serverMemberRole = new ServerMemberRole();

        serverMemberRole.setMember(member);
        serverMemberRole.setRole(role);
        serverMemberRole.setAssignedAt(LocalDateTime.now());
        serverMemberRole.setAssignedBy(userService.getUser(assignedById));

        return serverMemberRoleRepository.save(serverMemberRole);
    }

    public ServerMemberRole crateServerMemberRole(ServerMember member, ServerRole role, Long assignedById) {
        ServerMemberRole serverMemberRole = new ServerMemberRole();

        serverMemberRole.setMember(member);
        serverMemberRole.setRole(role);
        serverMemberRole.setAssignedAt(LocalDateTime.now());
        serverMemberRole.setAssignedBy(userService.getUser(assignedById));

        return serverMemberRoleRepository.save(serverMemberRole);
    }
}
