package com.zvonok.controller;

import com.zvonok.exception.InsufficientPermissionsException;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.model.Channel;
import com.zvonok.model.User;
import com.zvonok.security.dto.UserPrincipal;
import com.zvonok.service.ChannelFolderService;
import com.zvonok.service.ChannelService;
import com.zvonok.service.PermissionService;
import com.zvonok.service.ServerService;
import com.zvonok.service.UserService;
import com.zvonok.service.dto.CreateChannelDto;
import com.zvonok.service.dto.Permission;
import com.zvonok.service.dto.UpdateChannelDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-эндпоинты для управления каналами внутри конкретной папки сервера.
 * Для операций записи требуется право {@code MANAGE_CHANNELS}.
 */
@RestController
@RequestMapping("/server/{serverId}/channel-folders/{folderId}/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;
    private final ChannelFolderService channelFolderService;
    private final PermissionService permissionService;
    private final UserService userService;
    private final ServerService serverService;

    @GetMapping
    public ResponseEntity<List<Channel>> getChannels(
            @PathVariable Long serverId,
            @PathVariable Long folderId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);

        ensureServerExists(serverId);
        ensureFolderBelongsToServer(serverId, folderId);
        ensureCanViewFolder(userId, folderId);

        List<Channel> channels = channelService.getChannelsOrdered(folderId);
        return ResponseEntity.ok(channels);
    }

    @PostMapping
    public ResponseEntity<Channel> createChannel(
            @PathVariable Long serverId,
            @PathVariable Long folderId,
            @Valid @RequestBody CreateChannelDto createChannelDto,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);

        ensureServerExists(serverId);
        ensureFolderBelongsToServer(serverId, folderId);
        ensureCanManageChannels(userId, serverId);

        createChannelDto.setFolderId(folderId);
        Channel channel = channelService.createChannel(createChannelDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(channel);
    }

    @PutMapping("/{channelId}")
    public ResponseEntity<Channel> updateChannel(
            @PathVariable Long serverId,
            @PathVariable Long folderId,
            @PathVariable Long channelId,
            @Valid @RequestBody UpdateChannelDto updateChannelDto,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);

        ensureServerExists(serverId);
        ensureFolderBelongsToServer(serverId, folderId);
        ensureCanManageChannels(userId, serverId);
        channelService.getChannel(folderId, channelId);

        Channel updated = channelService.updateChannel(channelId, updateChannelDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(
            @PathVariable Long serverId,
            @PathVariable Long folderId,
            @PathVariable Long channelId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);

        ensureServerExists(serverId);
        ensureFolderBelongsToServer(serverId, folderId);
        ensureCanManageChannels(userId, serverId);
        channelService.getChannel(folderId, channelId);

        channelService.deleteChannel(channelId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId(UserPrincipal principal) {
        User user = userService.getUser(principal.getUsername());
        return user.getId();
    }

    private void ensureServerExists(Long serverId) {
        serverService.getServer(serverId);
    }

    private void ensureFolderBelongsToServer(Long serverId, Long folderId) {
        channelFolderService.getChannelFolderForServer(serverId, folderId);
    }

    private void ensureCanViewFolder(Long userId, Long folderId) {
        if (!permissionService.canUserViewFolder(userId, folderId)) {
            throw new InsufficientPermissionsException(
                    HttpResponseMessage.HTTP_INSUFFICIENT_PERMISSIONS_RESPONSE_MESSAGE.getMessage());
        }
    }

    private void ensureCanManageChannels(Long userId, Long serverId) {
        if (!permissionService.hasPermissionInServer(userId, serverId, Permission.MANAGE_CHANNELS)) {
            throw new InsufficientPermissionsException(
                    HttpResponseMessage.HTTP_INSUFFICIENT_PERMISSIONS_RESPONSE_MESSAGE.getMessage());
        }
    }
}

