package com.zvonok.controller;

import com.zvonok.model.User;
import com.zvonok.security.dto.UserPrincipal;
import com.zvonok.service.ServerService;
import com.zvonok.service.UserService;
import com.zvonok.service.dto.request.CreateServerRequest;
import com.zvonok.service.dto.request.UpdateServerRequest;
import com.zvonok.service.dto.request.UpdateServerMemberNicknameRequest;
import com.zvonok.service.dto.response.ServerResponse;
import com.zvonok.service.dto.response.ServerMemberResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/server")
@RequiredArgsConstructor
public class ServerController {

    private final ServerService serverService;
    private final UserService userService;

    /**
     * Создание нового сервера
     */
    @PostMapping("/create")
    public ResponseEntity<ServerResponse> createServer(
            @Valid @RequestBody CreateServerRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);
        ServerResponse response = serverService.createServer(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Получение серверов пользователя
     */
    @GetMapping("/my")
    public ResponseEntity<List<ServerResponse>> getMyServers(
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);
        List<ServerResponse> servers = serverService.getUserServers(userId);
        return ResponseEntity.ok(servers);
    }

    /**
     * Получение информации о сервере
     */
    @GetMapping("/{serverId}")
    public ResponseEntity<ServerResponse> getServer(
            @PathVariable Long serverId,
            @AuthenticationPrincipal UserPrincipal principal) {
//        Long userId = getCurrentUserId(principal);

        // Проверяем доступ к серверу
//        serverService.hasAccessToServer(userId, serverId);

        ServerResponse server = serverService.getServerResponse(serverId);
        return ResponseEntity.ok(server);
    }

    /**
     * Присоединение к серверу по invite коду
     */
    @PostMapping("/join/{inviteCode}")
    public ResponseEntity<ServerResponse> joinServer(
            @PathVariable String inviteCode,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);
        ServerResponse response = serverService.joinServerByInviteCode(inviteCode, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Обновление сервера
     */
    @PutMapping("/{serverId}")
    public ResponseEntity<ServerResponse> updateServer(
            @PathVariable Long serverId,
            @Valid @RequestBody UpdateServerRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);
        ServerResponse response = serverService.updateServer(serverId, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Регенерация invite кода
     */
    @PostMapping("/{serverId}/regenerate-invite")
    public ResponseEntity<Map<String, String>> regenerateInviteCode(
            @PathVariable Long serverId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);
        String newInviteCode = serverService.regenerateInviteCode(serverId, userId);
        return ResponseEntity.ok(Map.of("inviteCode", newInviteCode));
    }

    /**
     * Покинуть сервер
     */
    @PostMapping("/{serverId}/leave")
    public ResponseEntity<Void> leaveServer(
            @PathVariable Long serverId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);
        serverService.leaveServer(serverId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получение участников сервера
     */
    @GetMapping("/{serverId}/members")
    public ResponseEntity<List<ServerMemberResponse>> getServerMembers(
            @PathVariable Long serverId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);
        List<ServerMemberResponse> members = serverService.getServerMembers(serverId, userId);
        return ResponseEntity.ok(members);
    }

    /**
     * Исключение участника
     */
    @DeleteMapping("/{serverId}/members/{targetUserId}")
    public ResponseEntity<Void> kickMember(
            @PathVariable Long serverId,
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);
        serverService.kickMember(serverId, targetUserId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Обновление ника участника
     */
    @PatchMapping("/{serverId}/members/{targetUserId}/nickname")
    public ResponseEntity<ServerMemberResponse> updateMemberNickname(
            @PathVariable Long serverId,
            @PathVariable Long targetUserId,
            @Valid @RequestBody UpdateServerMemberNicknameRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);
        ServerMemberResponse response = serverService.updateMemberNickname(serverId, targetUserId, request.getNickname(), userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Удаление сервера
     */
    @DeleteMapping("/{serverId}")
    public ResponseEntity<Void> deleteServer(
            @PathVariable Long serverId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);
        serverService.deleteServer(serverId, userId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId(UserPrincipal principal) {
        User user = userService.getUser(principal.getUsername());
        return user.getId();
    }
}
