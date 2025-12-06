package com.zvonok.service;

import com.zvonok.exception.ServerMemberRoleNotFoundException;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.model.ServerMember;
import com.zvonok.model.ServerMemberRole;
import com.zvonok.model.ServerRole;
import com.zvonok.repository.ServerMemberRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing relationships between server members and roles.
 * Сервис для управления связями между участниками сервера и ролями.
 */
@RequiredArgsConstructor
@Service
public class ServerMemberRoleService {

    private final ServerMemberRoleRepository serverMemberRoleRepository;
    private final ServerMemberService serverMemberService;
    private final ServerRoleService serverRoleService;
    private final UserService userService;

    public ServerMemberRole getServerMemberRole(Long serverMemberRoleId) {
        return serverMemberRoleRepository.findById(serverMemberRoleId)
                .orElseThrow(() -> new ServerMemberRoleNotFoundException(
                        HttpResponseMessage.HTTP_SERVER_MEMBER_ROLE_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));
    }

    public ServerMemberRole createServerMemberRole(Long serverMemberId, Long serverRoleId, Long assignedById) {
        ServerMember member = serverMemberService.getServerMember(serverMemberId);
        ServerRole role = serverRoleService.getServerRole(serverRoleId);

        if (hasRoleAssigned(member.getId(), role.getId())) {
            return serverMemberRoleRepository.findByMemberIdAndRoleId(member.getId(), role.getId()).get();
        }

        ServerMemberRole serverMemberRole = new ServerMemberRole();
        serverMemberRole.setMember(member);
        serverMemberRole.setRole(role);
        serverMemberRole.setAssignedAt(LocalDateTime.now());
        serverMemberRole.setAssignedBy(userService.getUser(assignedById));

        ServerMemberRole saved = serverMemberRoleRepository.save(serverMemberRole);
        member.getMemberRoles().add(saved);
        return saved;
    }

    public ServerMemberRole createServerMemberRole(ServerMember member, ServerRole role, Long assignedById) {
        if (hasRoleAssigned(member.getId(), role.getId())) {
            return serverMemberRoleRepository.findByMemberIdAndRoleId(member.getId(), role.getId()).get();
        }

        ServerMemberRole serverMemberRole = new ServerMemberRole();
        serverMemberRole.setMember(member);
        serverMemberRole.setRole(role);
        serverMemberRole.setAssignedAt(LocalDateTime.now());
        serverMemberRole.setAssignedBy(userService.getUser(assignedById));

        ServerMemberRole saved = serverMemberRoleRepository.save(serverMemberRole);
        member.getMemberRoles().add(saved);
        return saved;
    }

    @Transactional
    public void removeRoleFromMember(Long memberId, Long roleId) {
        ServerMemberRole memberRole = serverMemberRoleRepository.findByMemberIdAndRoleId(memberId, roleId)
                .orElseThrow(() -> new ServerMemberRoleNotFoundException(
                        HttpResponseMessage.HTTP_SERVER_MEMBER_ROLE_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));
        memberRole.getMember().getMemberRoles().removeIf(mr -> mr.getId().equals(memberRole.getId()));
        serverMemberRoleRepository.delete(memberRole);
    }

    public List<Long> getMemberRoleIds(Long memberId) {
        return serverMemberRoleRepository.findByMemberId(memberId).stream()
                .map(memberRole -> memberRole.getRole().getId())
                .collect(Collectors.toList());
    }

    public boolean hasRoleAssigned(Long memberId, Long roleId) {
        return serverMemberRoleRepository.findByMemberIdAndRoleId(memberId, roleId).isPresent();
    }
}
