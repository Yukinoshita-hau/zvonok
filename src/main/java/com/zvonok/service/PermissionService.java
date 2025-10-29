package com.zvonok.service;

import com.zvonok.model.*;
import com.zvonok.repository.ChannelPermissionOverrideRepository;
import com.zvonok.repository.FolderPermissionOverrideRepository;
import com.zvonok.repository.ServerMemberRepository;
import com.zvonok.service.dto.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final ServerMemberRepository memberRepository;
    private final ChannelPermissionOverrideRepository channelOverrideRepository;
    private final FolderPermissionOverrideRepository folderOverrideRepository;
    private final ChannelService channelService;

    /**
     * Проверяет может ли пользователь видеть папку
     */
    public boolean canUserViewFolder(Long userId, Long folderId) {
        return hasPermissionInFolder(userId, folderId, Permission.VIEW_CHANNEL);
    }

    /**
     * Проверяет может ли пользователь видеть канал
     */
    public boolean canUserViewChannel(Long userId, Long channelId) {
        return hasPermissionInChannel(userId, channelId, Permission.VIEW_CHANNEL);
    }

    /**
     * Проверяет может ли пользователь отправлять сообщения в канал
     */
    public boolean canUserSendMessages(Long userId, Long channelId) {
        return hasPermissionInChannel(userId, channelId, Permission.SEND_MESSAGES);
    }

    public boolean hasPermissionInServer(Long userId, Long serverId, Permission permission) {
        Optional<ServerMember> optionalMember = memberRepository.findByUserIdAndServerId(userId, serverId);
        if (optionalMember.isEmpty() || !optionalMember.get().getIsActive()) {
            return false;
        }

        ServerMember member = optionalMember.get();
        List<ServerRole> userRoles = getUserRoles(member);

        // Админы могут все
        if (hasAdminRole(userRoles)) {
            return true;
        }

        return calculateServerPermissions(member, userRoles, permission);
    }

    /**
     * Основной метод проверки разрешений в канале
     */
    public boolean hasPermissionInChannel(Long userId, Long channelId, Permission permission) {
        Optional<ServerMember> optionalMember = memberRepository.findByUserIdAndChannelId(userId, channelId);
        if (optionalMember.isEmpty() || !optionalMember.get().getIsActive()) {
            return false;
        }
        ServerMember member = optionalMember.get();


        List<ServerRole> userRoles = getUserRoles(member);

        if (hasAdminRole(userRoles)) {
            return true;
        }

        return calculateChannelPermissions(member, userRoles, channelId, permission);
    }

    /**
     * Основной метод проверки разрешений в папке
     */
    public boolean hasPermissionInFolder(Long userId, Long folderId, Permission permission) {
        Optional<ServerMember> optionalMember = memberRepository.findByUserIdAndFolderId(userId, folderId);
        if (optionalMember.isEmpty() || !optionalMember.get().getIsActive()) {
            return false;
        }
        ServerMember member = optionalMember.get();


        List<ServerRole> userRoles = getUserRoles(member);

        if (hasAdminRole(userRoles)) {
            return true;
        }

        return calculateFolderPermissions(member, userRoles, folderId, permission);
    }

    public boolean isServerMember(Long userId, Long serverId) {
        Optional<ServerMember> member = memberRepository.findByUserIdAndServerId(userId, serverId);
        return member.isPresent() && member.get().getIsActive();
    }

    /**
     * Проверяет может ли пользователь управлять сервером
     */
    public boolean canManageServer(Long userId, Long serverId) {
        return hasPermissionInServer(userId, serverId, Permission.MANAGE_SERVER);
    }

    /**
     * Проверяет может ли пользователь исключать участников
     */
    public boolean canKickMembers(Long userId, Long serverId) {
        return hasPermissionInServer(userId, serverId, Permission.KICK_MEMBERS);
    }

    /**
     * Проверяет может ли пользователь банить участников
     */
    public boolean canBanMembers(Long userId, Long serverId) {
        return hasPermissionInServer(userId, serverId, Permission.BAN_MEMBERS);
    }

    /**
     * Проверяет может ли пользователь управлять ролями
     */
    public boolean canManageRoles(Long userId, Long serverId) {
        return hasPermissionInServer(userId, serverId, Permission.MANAGE_ROLES);
    }

    /**
     * Проверяет может ли пользователь создавать приглашения
     */
    public boolean canCreateInvites(Long userId, Long serverId) {
        return hasPermissionInServer(userId, serverId, Permission.CREATE_INVITE);
    }

    /**
     * Получить все разрешения пользователя на сервере
     */
    public Long getUserServerPermissions(Long userId, Long serverId) {
        Optional<ServerMember> optionalMember = memberRepository.findByUserIdAndServerId(userId, serverId);
        if (optionalMember.isEmpty() || !optionalMember.get().getIsActive()) {
            return 0L;
        }

        ServerMember member = optionalMember.get();
        List<ServerRole> userRoles = getUserRoles(member);

        // Если админ - все разрешения
        if (hasAdminRole(userRoles)) {
            return Permission.ADMINISTRATOR.getValue();
        }

        // Собираем все разрешения от ролей
        long permissions = 0L;
        for (ServerRole role : userRoles) {
            permissions |= role.getServerPermissions();
        }

        // Добавляем персональные разрешения
        permissions |= member.getPersonalPermissions();

        return permissions;
    }

    /**
     * Получить все роли пользователя на сервере
     */
    public List<ServerRole> getUserServerRoles(Long userId, Long serverId) {
        Optional<ServerMember> optionalMember = memberRepository.findByUserIdAndServerId(userId, serverId);
        if (optionalMember.isEmpty() || !optionalMember.get().getIsActive()) {
            return List.of();
        }

        return getUserRoles(optionalMember.get());
    }

    private boolean calculateServerPermissions(ServerMember member, List<ServerRole> roles, Permission permission) {
        Long permValue = permission.getValue();

        // Шаг 1: Базовые разрешения от ролей
        long serverPermissions = 0L;
        for (ServerRole role : roles) {
            serverPermissions |= role.getServerPermissions();
        }

        // Шаг 2: Персональные разрешения участника
        serverPermissions |= member.getPersonalPermissions();

        // Шаг 3: Проверка разрешения
        return (serverPermissions & permValue) != 0;
    }

    public boolean calculateChannelPermissions(ServerMember member, List<ServerRole> roles, Long channelId, Permission permission) {
        Long permValue = permission.getValue();

        // Шаг 1: Базовые разрешения от ролей
        Long basePermissions= 0L;
        for (ServerRole role: roles) {
            basePermissions |= role.getServerPermissions();
        }

        // Шаг 2: Разрешения папки (наследуются каналом)
        Channel channel = channelService.getChannel(channelId);
        ChannelFolder folder = channel.getFolder();


        long folderPermissions = calculateFolderPermissionsValue(member, roles, folder.getId());
        basePermissions |= folderPermissions;

        // Шаг 3: Переопределения канала для ролей
        List<ChannelPermissionOverride> roleOverrides =
                channelOverrideRepository.findByChannelIdAndRoleIn(channelId, roles);

        long channelAllowed = basePermissions;
        long channelDenied = 0L;

        for (ChannelPermissionOverride override : roleOverrides) {
            channelAllowed |= override.getAllowedPermissions();
            channelDenied |= override.getDeniedPermissions();
        }

        // Шаг 4: Персональные переопределения пользователя
        Optional<ChannelPermissionOverride> optionalChannelOverride =
                channelOverrideRepository.findByChannelIdAndUserId(channelId, member.getUser().getId());

        if (optionalChannelOverride.isPresent()) {
            ChannelPermissionOverride channelOverride = optionalChannelOverride.get();
            channelAllowed |= channelOverride.getAllowedPermissions();
            channelDenied |= channelOverride.getDeniedPermissions();
        }

        // Шаг 5: Персональные разрешения участника
        channelAllowed |= member.getPersonalPermissions();

        // Шаг 6: Финальная проверка (запреты приоритетнее)
        if ((channelDenied & permValue) != 0) {
            return false; // Явно запрещено
        }

        return (channelAllowed & permValue) != 0; // Разрешено
    }

    private boolean calculateFolderPermissions(ServerMember member, List<ServerRole> roles,
                                               Long folderId, Permission permission) {
        Long permValue = permission.getValue();

        // Шаг 1: Базовые разрешения от ролей
        long basePermissions = 0L;
        for (ServerRole role : roles) {
            basePermissions |= role.getServerPermissions();
        }

        // Шаг 2: Переопределения папки для ролей
        List<FolderPermissionOverride> roleOverrides =
                folderOverrideRepository.findByFolderIdAndRoleIn(folderId, roles);

        long folderAllowed = basePermissions;
        long folderDenied = 0L;

        for (FolderPermissionOverride override : roleOverrides) {
            folderAllowed |= override.getAllowedPermissions();
            folderDenied |= override.getDeniedPermissions();
        }

        // Шаг 3: Персональные переопределения пользователя для папки
        Optional<FolderPermissionOverride> userOverrideOpt =
                folderOverrideRepository.findByFolderIdAndUserId(folderId, member.getUser().getId());

        if (userOverrideOpt.isPresent()) {
            FolderPermissionOverride userOverride = userOverrideOpt.get();
            folderAllowed |= userOverride.getAllowedPermissions();
            folderDenied |= userOverride.getDeniedPermissions();
        }

        // Шаг 4: Персональные разрешения участника
        folderAllowed |= member.getPersonalPermissions();

        // Шаг 5: Финальная проверка (запреты приоритетнее)
        if ((folderDenied & permValue) != 0) {
            return false; // Явно запрещено
        }

        return (folderAllowed & permValue) != 0; // Разрешено
    }

    private long calculateFolderPermissionsValue(ServerMember member, List<ServerRole> roles, Long folderId) {
        // Аналогично calculateChannelPermissions, но для папки
        List<FolderPermissionOverride> roleOverrides =
                folderOverrideRepository.findByFolderIdAndRoleIn(folderId, roles);

        long allowed = 0L;
        long denied = 0L;

        for (FolderPermissionOverride override : roleOverrides) {
            allowed |= override.getAllowedPermissions();
            denied |= override.getDeniedPermissions();
        }

        // Персональные переопределения для папки
        Optional<FolderPermissionOverride> optionalFolderOverride =
                folderOverrideRepository.findByFolderIdAndUserId(folderId, member.getUser().getId());

        if (optionalFolderOverride.isPresent()) {
            FolderPermissionOverride folderOverride = optionalFolderOverride.get();
            allowed |= folderOverride.getAllowedPermissions();
            denied |= folderOverride.getDeniedPermissions();
        }

        return allowed & ~denied; // Возвращаем разрешения минус запреты
    }

    private List<ServerRole> getUserRoles(ServerMember member) {
        return member.getMemberRoles().stream()
                .map(ServerMemberRole::getRole)
                .filter(role -> role.getIsActive())
                .collect(Collectors.toList());
    }

    private boolean hasAdminRole(List<ServerRole> roles) {
        return roles.stream()
                .anyMatch(role -> Permission.hasPermission(role.getServerPermissions(), Permission.ADMINISTRATOR));
    }
}
