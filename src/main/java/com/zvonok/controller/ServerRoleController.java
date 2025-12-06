package com.zvonok.controller;

import com.zvonok.exception.InsufficientPermissionsException;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.model.Server;
import com.zvonok.model.ServerRole;
import com.zvonok.model.User;
import com.zvonok.security.dto.UserPrincipal;
import com.zvonok.service.PermissionService;
import com.zvonok.service.ServerRoleService;
import com.zvonok.service.ServerService;
import com.zvonok.service.UserService;
import com.zvonok.service.dto.CreateServerRoleDto;
import com.zvonok.service.dto.UpdateServerRoleDto;
import com.zvonok.service.dto.request.CreateServerRoleRequest;
import com.zvonok.service.dto.request.UpdateServerRoleRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-эндпоинты управления ролями сервера.
 * Требует права {@code MANAGE_ROLES} для операций записи.
 */
@RestController
@RequestMapping("/server/{serverId}/roles")
@RequiredArgsConstructor
public class ServerRoleController {

    private final ServerRoleService serverRoleService;
    private final ServerService serverService;
    private final PermissionService permissionService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<ServerRole>> getServerRoles(
            @PathVariable Long serverId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);

        ensureServerExists(serverId);
        ensureIsServerMember(userId, serverId);

        List<ServerRole> roles = serverRoleService.getActiveServerRoles(serverId);
        return ResponseEntity.ok(roles);
    }

    @PostMapping
    public ResponseEntity<ServerRole> createServerRole(
            @PathVariable Long serverId,
            @Valid @RequestBody CreateServerRoleRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);

        ensureServerExists(serverId);
        ensureCanManageRoles(userId, serverId);

        Server server = serverService.getServer(serverId);

        CreateServerRoleDto dto = new CreateServerRoleDto();
        dto.setName(request.getName());
        dto.setColor(request.getColor());
        dto.setPosition(request.getPosition());
        dto.setServerPermissions(request.getServerPermissions());
        dto.setMentionable(request.isMentionable());
        dto.setEveryone(false);
        dto.setServer(server);

        ServerRole role = serverRoleService.createServerRole(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<ServerRole> updateServerRole(
            @PathVariable Long serverId,
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateServerRoleRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);

        ensureServerExists(serverId);
        ensureCanManageRoles(userId, serverId);
        serverRoleService.getServerRoleForServer(serverId, roleId);

        UpdateServerRoleDto dto = new UpdateServerRoleDto();
        dto.setName(request.getName());
        dto.setColor(request.getColor());
        dto.setPosition(request.getPosition());
        dto.setServerPermissions(request.getServerPermissions());
        dto.setMentionable(request.getMentionable());
        dto.setActive(request.getActive());

        ServerRole updated = serverRoleService.updateServerRole(roleId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteServerRole(
            @PathVariable Long serverId,
            @PathVariable Long roleId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);

        ensureServerExists(serverId);
        ensureCanManageRoles(userId, serverId);
        serverRoleService.getServerRoleForServer(serverId, roleId);

        serverRoleService.deleteServerRole(roleId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId(UserPrincipal principal) {
        User user = userService.getUser(principal.getUsername());
        return user.getId();
    }

    private void ensureServerExists(Long serverId) {
        serverService.getServer(serverId);
    }

    private void ensureIsServerMember(Long userId, Long serverId) {
        if (!permissionService.isServerMember(userId, serverId)) {
            throw new InsufficientPermissionsException(
                    HttpResponseMessage.HTTP_INSUFFICIENT_PERMISSIONS_RESPONSE_MESSAGE.getMessage());
        }
    }

    private void ensureCanManageRoles(Long userId, Long serverId) {
        if (!permissionService.canManageRoles(userId, serverId)) {
            throw new InsufficientPermissionsException(
                    HttpResponseMessage.HTTP_INSUFFICIENT_PERMISSIONS_RESPONSE_MESSAGE.getMessage());
        }
    }
}

