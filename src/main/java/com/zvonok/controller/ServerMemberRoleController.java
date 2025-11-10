package com.zvonok.controller;

import com.zvonok.exception.InsufficientPermissionsException;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.model.ServerMember;
import com.zvonok.model.User;
import com.zvonok.security.dto.UserPrincipal;
import com.zvonok.service.PermissionService;
import com.zvonok.service.ServerMemberRoleService;
import com.zvonok.service.ServerMemberService;
import com.zvonok.service.ServerRoleService;
import com.zvonok.service.ServerService;
import com.zvonok.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST-эндпоинты для назначения и снятия ролей у участника сервера.
 */
@RestController
@RequestMapping("/server/{serverId}/members/{memberId}/roles")
@RequiredArgsConstructor
public class ServerMemberRoleController {

    private final ServerMemberRoleService serverMemberRoleService;
    private final ServerMemberService serverMemberService;
    private final ServerRoleService serverRoleService;
    private final ServerService serverService;
    private final PermissionService permissionService;
    private final UserService userService;

    /**
     * Назначает участнику сервера указанную роль.
     *
     * @param serverId   идентификатор сервера
     * @param memberId   идентификатор участника
     * @param roleId     идентификатор роли
     * @param principal  текущий пользователь
     * @return пустой ответ {@code 204}
     */
    @PostMapping("/{roleId}")
    public ResponseEntity<Void> assignRoleToMember(
            @PathVariable Long serverId,
            @PathVariable Long memberId,
            @PathVariable Long roleId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);

        ensureServerExists(serverId);
        ensureCanManageRoles(userId, serverId);
        getServerMemberForServer(serverId, memberId);
        serverRoleService.getServerRoleForServer(serverId, roleId);

        serverMemberRoleService.createServerMemberRole(memberId, roleId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Снимает роль с участника сервера.
     *
     * @param serverId   идентификатор сервера
     * @param memberId   идентификатор участника
     * @param roleId     идентификатор роли
     * @param principal  текущий пользователь
     * @return пустой ответ {@code 204}
     */
    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> removeRoleFromMember(
            @PathVariable Long serverId,
            @PathVariable Long memberId,
            @PathVariable Long roleId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);

        ensureServerExists(serverId);
        ensureCanManageRoles(userId, serverId);
        ServerMember member = getServerMemberForServer(serverId, memberId);
        serverRoleService.getServerRoleForServer(serverId, roleId);

        serverMemberRoleService.removeRoleFromMember(member.getId(), roleId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId(UserPrincipal principal) {
        User user = userService.getUser(principal.getUsername());
        return user.getId();
    }

    private void ensureServerExists(@NotNull Long serverId) {
        serverService.getServer(serverId);
    }

    private ServerMember getServerMemberForServer(Long serverId, Long memberId) {
        ServerMember member = serverMemberService.getServerMember(memberId);
        if (!member.getServer().getId().equals(serverId)) {
            throw new InsufficientPermissionsException(
                    HttpResponseMessage.HTTP_INSUFFICIENT_PERMISSIONS_RESPONSE_MESSAGE.getMessage());
        }
        return member;
    }

    private void ensureCanManageRoles(Long userId, Long serverId) {
        if (!permissionService.canManageRoles(userId, serverId)) {
            throw new InsufficientPermissionsException(
                    HttpResponseMessage.HTTP_INSUFFICIENT_PERMISSIONS_RESPONSE_MESSAGE.getMessage());
        }
    }
}

