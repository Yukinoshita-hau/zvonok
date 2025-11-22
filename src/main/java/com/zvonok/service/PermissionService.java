package com.zvonok.service;

import com.zvonok.model.*;
import com.zvonok.repository.ChannelPermissionOverrideRepository;
import com.zvonok.repository.FolderPermissionOverrideRepository;
import com.zvonok.repository.ServerMemberRepository;
import com.zvonok.service.dto.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing and checking user permissions in servers, folders, and channels.
 * Implements a hierarchical permission system with role-based access control and permission overrides.
 *
 * Сервис для управления и проверки прав пользователей в серверах, папках и каналах.
 * Реализует иерархическую систему разрешений с контролем доступа на основе ролей и переопределениями прав.
 */
@Service
public class PermissionService {

    private final ServerMemberRepository memberRepository;
    private final ChannelPermissionOverrideRepository channelOverrideRepository;
    private final FolderPermissionOverrideRepository folderOverrideRepository;
    private final ChannelService channelService;

    public PermissionService(
            ServerMemberRepository memberRepository,
            ChannelPermissionOverrideRepository channelOverrideRepository,
            FolderPermissionOverrideRepository folderOverrideRepository,
            @Lazy ChannelService channelService) {
        this.memberRepository = memberRepository;
        this.channelOverrideRepository = channelOverrideRepository;
        this.folderOverrideRepository = folderOverrideRepository;
        this.channelService = channelService;
    }

    /**
     * Checks if a user can view a channel folder.
     * This is a convenience method that calls hasPermissionInFolder with VIEW_CHANNEL permission.
     *
     * Проверяет, может ли пользователь просматривать папку каналов.
     * Это удобный метод, который вызывает hasPermissionInFolder с правом VIEW_CHANNEL.
     *
     * @param userId    the unique identifier of the user
     *                  уникальный идентификатор пользователя
     * @param folderId  the unique identifier of the folder
     *                  уникальный идентификатор папки
     * @return true if user can view the folder, false otherwise
     *         true, если пользователь может просматривать папку, иначе false
     */
    public boolean canUserViewFolder(Long userId, Long folderId) {
        return hasPermissionInFolder(userId, folderId, Permission.VIEW_CHANNEL);
    }

    /**
     * Checks if a user can view a channel.
     * This is a convenience method that calls hasPermissionInChannel with VIEW_CHANNEL permission.
     *
     * Проверяет, может ли пользователь просматривать канал.
     * Это удобный метод, который вызывает hasPermissionInChannel с правом VIEW_CHANNEL.
     *
     * @param userId     the unique identifier of the user
     *                   уникальный идентификатор пользователя
     * @param channelId  the unique identifier of the channel
     *                   уникальный идентификатор канала
     * @return true if user can view the channel, false otherwise
     *         true, если пользователь может просматривать канал, иначе false
     */
    public boolean canUserViewChannel(Long userId, Long channelId) {
        return hasPermissionInChannel(userId, channelId, Permission.VIEW_CHANNEL);
    }

    /**
     * Checks if a user can send messages to a channel.
     * This is a convenience method that calls hasPermissionInChannel with SEND_MESSAGES permission.
     *
     * Проверяет, может ли пользователь отправлять сообщения в канал.
     * Это удобный метод, который вызывает hasPermissionInChannel с правом SEND_MESSAGES.
     *
     * @param userId     the unique identifier of the user
     *                   уникальный идентификатор пользователя
     * @param channelId  the unique identifier of the channel
     *                   уникальный идентификатор канала
     * @return true if user can send messages, false otherwise
     *         true, если пользователь может отправлять сообщения, иначе false
     */
    public boolean canUserSendMessages(Long userId, Long channelId) {
        return hasPermissionInChannel(userId, channelId, Permission.SEND_MESSAGES);
    }

    /**
     * Checks if a user has a specific permission in a server.
     * Validates that the user is an active server member, then checks permissions
     * considering roles, personal permissions, and admin status.
     *
     * Проверяет, имеет ли пользователь конкретное право в сервере.
     * Проверяет, что пользователь является активным участником сервера, затем проверяет права
     * с учетом ролей, персональных прав и статуса администратора.
     *
     * @param userId      the unique identifier of the user
     *                    уникальный идентификатор пользователя
     * @param serverId    the unique identifier of the server
     *                    уникальный идентификатор сервера
     * @param permission  the permission to check
     *                    право для проверки
     * @return true if user has the permission, false otherwise
     *         true, если у пользователя есть право, иначе false
     */
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
     * Main method for checking permissions in a channel.
     * Validates that the user is an active member, then calculates permissions considering:
     * role permissions, folder permissions, channel overrides, and personal permissions.
     *
     * Основной метод проверки прав в канале.
     * Проверяет, что пользователь является активным участником, затем рассчитывает права с учетом:
     * прав ролей, прав папки, переопределений канала и персональных прав.
     *
     * @param userId      the unique identifier of the user
     *                    уникальный идентификатор пользователя
     * @param channelId   the unique identifier of the channel
     *                    уникальный идентификатор канала
     * @param permission  the permission to check
     *                    право для проверки
     * @return true if user has the permission, false otherwise
     *         true, если у пользователя есть право, иначе false
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
     * Main method for checking permissions in a folder.
     * Validates that the user is an active member, then calculates permissions considering:
     * role permissions, folder overrides, and personal permissions.
     *
     * Основной метод проверки прав в папке.
     * Проверяет, что пользователь является активным участником, затем рассчитывает права с учетом:
     * прав ролей, переопределений папки и персональных прав.
     *
     * @param userId      the unique identifier of the user
     *                    уникальный идентификатор пользователя
     * @param folderId    the unique identifier of the folder
     *                    уникальный идентификатор папки
     * @param permission  the permission to check
     *                    право для проверки
     * @return true if user has the permission, false otherwise
     *         true, если у пользователя есть право, иначе false
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

    /**
     * Checks if a user is an active member of a server.
     *
     * Проверяет, является ли пользователь активным участником сервера.
     *
     * @param userId    the unique identifier of the user
     *                  уникальный идентификатор пользователя
     * @param serverId  the unique identifier of the server
     *                  уникальный идентификатор сервера
     * @return true if user is an active server member, false otherwise
     *         true, если пользователь является активным участником сервера, иначе false
     */
    public boolean isServerMember(Long userId, Long serverId) {
        Optional<ServerMember> member = memberRepository.findByUserIdAndServerId(userId, serverId);
        return member.isPresent() && member.get().getIsActive();
    }

    /**
     * Checks if a user can manage a server.
     * This is a convenience method that calls hasPermissionInServer with MANAGE_SERVER permission.
     *
     * Проверяет, может ли пользователь управлять сервером.
     * Это удобный метод, который вызывает hasPermissionInServer с правом MANAGE_SERVER.
     *
     * @param userId    the unique identifier of the user
     *                  уникальный идентификатор пользователя
     * @param serverId  the unique identifier of the server
     *                  уникальный идентификатор сервера
     * @return true if user can manage the server, false otherwise
     *         true, если пользователь может управлять сервером, иначе false
     */
    public boolean canManageServer(Long userId, Long serverId) {
        return hasPermissionInServer(userId, serverId, Permission.MANAGE_SERVER);
    }

    /**
     * Checks if a user can kick members from a server.
     * This is a convenience method that calls hasPermissionInServer with KICK_MEMBERS permission.
     *
     * Проверяет, может ли пользователь исключать участников из сервера.
     * Это удобный метод, который вызывает hasPermissionInServer с правом KICK_MEMBERS.
     *
     * @param userId    the unique identifier of the user
     *                  уникальный идентификатор пользователя
     * @param serverId  the unique identifier of the server
     *                  уникальный идентификатор сервера
     * @return true if user can kick members, false otherwise
     *         true, если пользователь может исключать участников, иначе false
     */
    public boolean canKickMembers(Long userId, Long serverId) {
        return hasPermissionInServer(userId, serverId, Permission.KICK_MEMBERS);
    }

    /**
     * Checks if a user can ban members from a server.
     * This is a convenience method that calls hasPermissionInServer with BAN_MEMBERS permission.
     *
     * Проверяет, может ли пользователь банить участников сервера.
     * Это удобный метод, который вызывает hasPermissionInServer с правом BAN_MEMBERS.
     *
     * @param userId    the unique identifier of the user
     *                  уникальный идентификатор пользователя
     * @param serverId  the unique identifier of the server
     *                  уникальный идентификатор сервера
     * @return true if user can ban members, false otherwise
     *         true, если пользователь может банить участников, иначе false
     */
    public boolean canBanMembers(Long userId, Long serverId) {
        return hasPermissionInServer(userId, serverId, Permission.BAN_MEMBERS);
    }

    /**
     * Checks if a user can manage roles in a server.
     * This is a convenience method that calls hasPermissionInServer with MANAGE_ROLES permission.
     *
     * Проверяет, может ли пользователь управлять ролями в сервере.
     * Это удобный метод, который вызывает hasPermissionInServer с правом MANAGE_ROLES.
     *
     * @param userId    the unique identifier of the user
     *                  уникальный идентификатор пользователя
     * @param serverId  the unique identifier of the server
     *                  уникальный идентификатор сервера
     * @return true if user can manage roles, false otherwise
     *         true, если пользователь может управлять ролями, иначе false
     */
    public boolean canManageRoles(Long userId, Long serverId) {
        return hasPermissionInServer(userId, serverId, Permission.MANAGE_ROLES);
    }

    /**
     * Checks if a user can create invite codes for a server.
     * This is a convenience method that calls hasPermissionInServer with CREATE_INVITE permission.
     *
     * Проверяет, может ли пользователь создавать коды приглашения для сервера.
     * Это удобный метод, который вызывает hasPermissionInServer с правом CREATE_INVITE.
     *
     * @param userId    the unique identifier of the user
     *                  уникальный идентификатор пользователя
     * @param serverId  the unique identifier of the server
     *                  уникальный идентификатор сервера
     * @return true if user can create invites, false otherwise
     *         true, если пользователь может создавать приглашения, иначе false
     */
    public boolean canCreateInvites(Long userId, Long serverId) {
        return hasPermissionInServer(userId, serverId, Permission.CREATE_INVITE);
    }

    /**
     * Gets all permissions of a user in a server.
     * Combines all role permissions and personal permissions into a single permission value.
     * If the user has admin role, returns all administrator permissions.
     *
     * Получает все права пользователя в сервере.
     * Объединяет все права ролей и персональные права в одно значение прав.
     * Если у пользователя есть роль администратора, возвращает все права администратора.
     *
     * @param userId    the unique identifier of the user
     *                  уникальный идентификатор пользователя
     * @param serverId  the unique identifier of the server
     *                  уникальный идентификатор сервера
     * @return combined permission value (0L if user is not a member or not active)
     *         объединенное значение прав (0L, если пользователь не является участником или не активен)
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
     * Gets all roles of a user in a server.
     * Returns only active roles assigned to the user.
     *
     * Получает все роли пользователя в сервере.
     * Возвращает только активные роли, назначенные пользователю.
     *
     * @param userId    the unique identifier of the user
     *                  уникальный идентификатор пользователя
     * @param serverId  the unique identifier of the server
     *                  уникальный идентификатор сервера
     * @return List of active ServerRole entities (empty list if user is not a member or not active)
     *         список активных сущностей ServerRole (пустой список, если пользователь не является участником или не активен)
     */
    public List<ServerRole> getUserServerRoles(Long userId, Long serverId) {
        Optional<ServerMember> optionalMember = memberRepository.findByUserIdAndServerId(userId, serverId);
        if (optionalMember.isEmpty() || !optionalMember.get().getIsActive()) {
            return List.of();
        }

        return getUserRoles(optionalMember.get());
    }

    // ===== PRIVATE HELPER METHODS =====

    /**
     * Calculates server-level permissions for a member based on roles and personal permissions.
     *
     * Рассчитывает права уровня сервера для участника на основе ролей и персональных прав.
     *
     * @param member     the server member
     *                   участник сервера
     * @param roles      list of user's roles
     *                   список ролей пользователя
     * @param permission the permission to check
     *                   право для проверки
     * @return true if member has the permission, false otherwise
     *         true, если участник имеет право, иначе false
     */
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

    /**
     * Calculates channel-level permissions for a member.
     * Considers: role permissions, folder permissions (inherited), channel overrides, and personal permissions.
     * Denies have priority over allows.
     *
     * Рассчитывает права уровня канала для участника.
     * Учитывает: права ролей, права папки (наследуются), переопределения канала и персональные права.
     * Запреты имеют приоритет над разрешениями.
     *
     * @param member     the server member
     *                   участник сервера
     * @param roles      list of user's roles
     *                   список ролей пользователя
     * @param channelId  the unique identifier of the channel
     *                   уникальный идентификатор канала
     * @param permission the permission to check
     *                   право для проверки
     * @return true if member has the permission, false otherwise
     *         true, если участник имеет право, иначе false
     */
    public boolean calculateChannelPermissions(ServerMember member, List<ServerRole> roles, Long channelId, Permission permission) {
        Long permValue = permission.getValue();

        // Шаг 1: Базовые разрешения от ролей
        Long basePermissions = 0L;
        for (ServerRole role : roles) {
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

    /**
     * Calculates folder-level permissions for a member.
     * Considers: role permissions, folder overrides, and personal permissions.
     * Denies have priority over allows.
     *
     * Рассчитывает права уровня папки для участника.
     * Учитывает: права ролей, переопределения папки и персональные права.
     * Запреты имеют приоритет над разрешениями.
     *
     * @param member     the server member
     *                   участник сервера
     * @param roles      list of user's roles
     *                   список ролей пользователя
     * @param folderId   the unique identifier of the folder
     *                   уникальный идентификатор папки
     * @param permission the permission to check
     *                   право для проверки
     * @return true if member has the permission, false otherwise
     *         true, если участник имеет право, иначе false
     */
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

    /**
     * Calculates the permission value for a folder (used when calculating channel permissions).
     * Returns allowed permissions minus denied permissions.
     *
     * Рассчитывает значение прав для папки (используется при расчете прав канала).
     * Возвращает разрешенные права минус запрещенные права.
     *
     * @param member   the server member
     *                 участник сервера
     * @param roles    list of user's roles
     *                 список ролей пользователя
     * @param folderId the unique identifier of the folder
     *                 уникальный идентификатор папки
     * @return permission value (allowed permissions minus denied permissions)
     *         значение прав (разрешенные права минус запрещенные права)
     */
    private long calculateFolderPermissionsValue(ServerMember member, List<ServerRole> roles, Long folderId) {
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

    /**
     * Gets all active roles assigned to a server member.
     *
     * Получает все активные роли, назначенные участнику сервера.
     *
     * @param member  the server member
     *                участник сервера
     * @return List of active ServerRole entities
     *         список активных сущностей ServerRole
     */
    private List<ServerRole> getUserRoles(ServerMember member) {
        return member.getMemberRoles().stream()
                .map(ServerMemberRole::getRole)
                .filter(ServerRole::getIsActive)
                .collect(Collectors.toList());
    }

    /**
     * Checks if any of the roles has administrator permission.
     *
     * Проверяет, имеет ли любая из ролей право администратора.
     *
     * @param roles  list of roles to check
     *               список ролей для проверки
     * @return true if any role has administrator permission, false otherwise
     *         true, если любая роль имеет право администратора, иначе false
     */
    private boolean hasAdminRole(List<ServerRole> roles) {
        return roles.stream()
                .anyMatch(role -> Permission.hasPermission(role.getServerPermissions(), Permission.ADMINISTRATOR));
    }
}
