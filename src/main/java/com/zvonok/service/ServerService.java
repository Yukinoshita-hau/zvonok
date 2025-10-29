package com.zvonok.service;

import com.zvonok.exception.*;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.model.*;
import com.zvonok.model.enumeration.ChannelType;
import com.zvonok.repository.*;
import com.zvonok.service.dto.CreateChannelDto;
import com.zvonok.service.dto.CreateChannelFolderDto;
import com.zvonok.service.dto.CreateServerRoleDto;
import com.zvonok.service.dto.Permission;
import com.zvonok.service.dto.request.CreateServerRequest;
import com.zvonok.service.dto.request.UpdateServerRequest;
import com.zvonok.service.dto.response.ServerResponse;
import com.zvonok.service.dto.response.ServerMemberResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServerService {

    private final ServerRepository serverRepository;
    private final UserService userService;
    private final InviteCodeService inviteCodeService;
    private final PermissionService permissionService;
    private final ServerMemberService serverMemberService;
    private final ServerMemberRoleService serverMemberRoleService;
    private final ServerRoleService serverRoleService;
    private final ChannelService channelService;
    private final ChannelFolderService channelFolderService;

    /**
     * Создание нового сервера
     */
    @Transactional
    public ServerResponse createServer(CreateServerRequest request, Long ownerId) {
        User owner = userService.getUser(ownerId);

        // Создаем сервер
        Server server = new Server();
        server.setName(request.getName());
        server.setInvitedCode(inviteCodeService.generateUniqueInviteCode());
        server.setOwner(owner);
        server.setMaxMember(request.getMaxMembers() != null ? request.getMaxMembers() : 1000);
        server.setCreatedAt(LocalDateTime.now());

        Server savedServer = serverRepository.save(server);

        // Создаем роли по умолчанию
        ServerRole everyoneRole = createEveryoneRole(savedServer);
        ServerRole ownerRole = createOwnerRole(savedServer);

        // Добавляем владельца как участника
        ServerMember ownerMember = addOwnerAsMember(savedServer, owner);
        assignRoleToMember(ownerMember, ownerRole, ownerId);

        // Создаем дефолтную папку и каналы
        ChannelFolder defaultFolder = createDefaultFolder(savedServer.getId());
        createDefaultChannels(defaultFolder.getId());

        log.info("Server '{}' created successfully with ID {}", savedServer.getName(), savedServer.getId());
        return mapToResponse(savedServer);
    }

    /**
     * Получение сервера по ID
     */
    public Server getServer(Long serverId) {
        return serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFoundException("Сервер не найден с ID: " + serverId));
    }

    /**
     * Получение серверов пользователя
     */
    public List<ServerResponse> getUserServers(Long userId) {
        List<Server> servers = serverRepository.findServersByUserId(userId);
        return servers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public boolean hasAccessToServer(Long userId, Long serverId) {
        return permissionService.isServerMember(userId, serverId);
    }

    public void hasAccessToServerAndThrowExceptionIfFalse(Long userId, Long serverId) {
        if (!hasAccessToServer(userId, serverId)) {
            throw new InsufficientPermissionsException(HttpResponseMessage.HTTP_INSUFFICIENT_PERMISSIONS_RESPONSE_MESSAGE.getMessage());
        }
    }

    public ServerResponse getServerResponse(Long serverId) {
        Server server = getServer(serverId);
        return mapToResponse(server);
    }

    /**
     * Присоединение к серверу по invite коду
     */
    @Transactional
    public ServerResponse joinServerByInviteCode(String inviteCode, Long userId) {
        log.info("User {} attempting to join server with invite code: {}", userId, inviteCode);

        Server server = serverRepository.findByInvitedCode(inviteCode)
                .orElseThrow(() -> new ServerNotFoundException(HttpResponseMessage.HTTP_SERVER_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));

        if (!server.getIsActive()) {
            throw new ServerNotFoundException(HttpResponseMessage.HTTP_SERVER_NOT_ACTIVE_RESPONSE_MESSAGE.getMessage());
        }

        User user = userService.getUser(userId);

        // Проверяем не является ли уже участником
        if (isHeServerMember(userId, server)) {
            return mapToResponse(server); // Уже участник
        }

        // Проверяем лимит участников
        long memberCount = serverMemberService.countServerMembers(server.getId());
        if (memberCount >= server.getMaxMember()) {
            throw new IllegalStateException("Достигнут максимум участников сервера");
        }

        // Добавляем как участника
        ServerMember newMember = addUserAsMember(server, user);

        // Назначаем роль x@everyone
        ServerRole everyoneRole = serverRoleService.getServerRoleWithIsEveryoneTrue(server.getId());

        assignRoleToMember(newMember, everyoneRole, userId);

        log.info("User {} successfully joined server {}", userId, server.getId());
        return mapToResponse(server);
    }

    public boolean isHeServerMember(Long userId, Server server) {
        return serverMemberService.getServerMember(userId, server.getId()) != null;
    }

    /**
     * Обновление сервера
     */
    @Transactional
    public ServerResponse updateServer(Long serverId, UpdateServerRequest request, Long userId) {
        Server server = getServer(serverId);

        // Проверяем права на управление сервером
        if (!canManageServer(userId, serverId)) {
            throw new InsufficientPermissionsException(HttpResponseMessage.HTTP_INSUFFICIENT_PERMISSIONS_RESPONSE_MESSAGE.getMessage());
        }

        if (request.getName() != null) {
            server.setName(request.getName());
        }

        if (request.getMaxMembers() != null) {
            server.setMaxMember(request.getMaxMembers());
        }

        Server updatedServer = serverRepository.save(server);
        log.info("Server {} updated by user {}", serverId, userId);

        return mapToResponse(updatedServer);
    }

    /**
     * Регенерация invite кода
     */
    @Transactional
    public String regenerateInviteCode(Long serverId, Long userId) {
        Server server = getServer(serverId);

        if (!canManageServer(userId, serverId)) {
            throw new InsufficientPermissionsException(HttpResponseMessage.HTTP_INSUFFICIENT_PERMISSIONS_RESPONSE_MESSAGE.getMessage());
        }

        String newInviteCode = inviteCodeService.generateUniqueInviteCode();
        server.setInvitedCode(newInviteCode);
        serverRepository.save(server);

        log.info("Invite code regenerated for server {} by user {}", serverId, userId);
        return newInviteCode;
    }

    /**
     * Покинуть сервер
     */
    @Transactional
    public void leaveServer(Long serverId, Long userId) {
        Server server = getServer(serverId);

        // Владелец не может покинуть сервер
        if (server.getOwner().getId().equals(userId)) {
            throw new OwnerCanNotLeaveServerException(HttpResponseMessage.HTTP_OWNER_CAN_NOT_LEAVE_SERVER_RESPONSE_MESSAGE.getMessage());
        }

        ServerMember member = serverMemberService.getServerMember(userId, serverId);

        member.setIsActive(false);
        member.setLeftAt(LocalDateTime.now());
        serverMemberService.updateServerMember(member);

        log.info("User {} left server {}", userId, serverId);
    }

    /**
     * Получение участников сервера
     */
    public List<ServerMemberResponse> getServerMembers(Long serverId, Long userId) {
        // Проверяем может ли пользователь видеть участников
        if (!canViewMembers(userId, serverId)) {
            throw new InsufficientPermissionsException(HttpResponseMessage.HTTP_INSUFFICIENT_PERMISSIONS_RESPONSE_MESSAGE.getMessage());
        }

        List<ServerMember> members = serverMemberService.getAllActiveMember(serverId);
        return members.stream()
                .map(this::mapToMemberResponse)
                .collect(Collectors.toList());
    }

    /**
     * Исключение участника
     */
    @Transactional
    public void kickMember(Long serverId, Long targetUserId, Long kickerUserId) {
        Server server = getServer(serverId);

        // Проверяем права на исключение
        if (!canKickMembers(kickerUserId, serverId)) {
            throw new InsufficientPermissionsException("Недостаточно прав для исключения участников");
        }

        // Нельзя исключить владельца
        if (server.getOwner().getId().equals(targetUserId)) {
            throw new IllegalStateException("Нельзя исключить владельца сервера");
        }

        // Нельзя исключить самого себя
        if (kickerUserId.equals(targetUserId)) {
            throw new IllegalStateException("Нельзя исключить самого себя");
        }

        ServerMember targetMember = serverMemberService.getServerMember(targetUserId, serverId);

        targetMember.setIsActive(false);
        targetMember.setLeftAt(LocalDateTime.now());
        serverMemberService.updateServerMember(targetMember);

        log.info("User {} kicked from server {} by user {}", targetUserId, serverId, kickerUserId);
    }

    // ===== ПРИВАТНЫЕ МЕТОДЫ =====

    private ServerRole createEveryoneRole(Server server) {
        CreateServerRoleDto createDto = new CreateServerRoleDto();
        createDto.setName("everyone");
        createDto.setColor("#ffffff");
        createDto.setPosition(0);
        createDto.setServerPermissions(Permission.VIEW_CHANNEL.getValue() |
                Permission.SEND_MESSAGES.getValue() |
                Permission.READ_MESSAGE_HISTORY.getValue());
        createDto.setEveryone(true);
        createDto.setMentionable(false);
        createDto.setServer(server);

        return serverRoleService.createServerRole(createDto);
    }

    private ServerRole createOwnerRole(Server server) {
        CreateServerRoleDto createDto = new CreateServerRoleDto();
        createDto.setName("Owner");
        createDto.setColor("#ff0000");
        createDto.setPosition(1000);
        createDto.setServerPermissions(Permission.ADMINISTRATOR.getValue());
        createDto.setEveryone(false);
        createDto.setMentionable(true);
        createDto.setServer(server);

        return serverRoleService.createServerRole(createDto);
    }

    private ServerMember addOwnerAsMember(Server server, User owner) {
        return serverMemberService.createServerMember(server, owner);
    }

    private ServerMember addUserAsMember(Server server, User user) {
        return serverMemberService.createServerMember(server, user);
    }

    private void assignRoleToMember(ServerMember member, ServerRole role, Long assignedById) {
        serverMemberRoleService.crateServerMemberRole(member, role, assignedById);
    }

    private ChannelFolder createDefaultFolder(Long serverId) {
        CreateChannelFolderDto folder = new CreateChannelFolderDto();
        folder.setName("Основные каналы");
        folder.setServerId(serverId);
        folder.setPosition(0);


        return channelFolderService.createChannelFolder(folder);
    }

    private void createDefaultChannels(Long folderId) {

        // Общий канал
        CreateChannelDto generalChannel = new CreateChannelDto();
        generalChannel.setName("общий");
        generalChannel.setFolderId(folderId);
        generalChannel.setType(ChannelType.TEXT);
        generalChannel.setPosition(0);
        generalChannel.setUserLimit(10000);
        generalChannel.setTopic("Добро пожаловать на сервер!");

        // Голосовой канал
        CreateChannelDto voiceChannel = new CreateChannelDto();
        voiceChannel.setName("Голосовой канал");
        voiceChannel.setFolderId(folderId);
        voiceChannel.setType(ChannelType.VOICE);
        voiceChannel.setPosition(1);
        voiceChannel.setUserLimit(15);

        channelService.createChannel(generalChannel);
        channelService.createChannel(voiceChannel);
    }

    // ===== ПРОВЕРКИ ПРАВ =====

    private boolean canManageServer(Long userId, Long serverId) {
        return permissionService.hasPermissionInServer(userId, serverId, Permission.MANAGE_SERVER) ||
                serverRepository.isServerOwner(userId, serverId);
    }

    private boolean canViewMembers(Long userId, Long serverId) {
        return serverMemberService.getServerMember(userId, serverId) != null;
    }

    private boolean canKickMembers(Long userId, Long serverId) {
        return permissionService.hasPermissionInServer(userId, serverId, Permission.KICK_MEMBERS) ||
                serverRepository.isServerOwner(userId, serverId);
    }

    // ===== МАППИНГ =====

    public ServerResponse mapToResponse(Server server) {
        return ServerResponse.builder()
                .id(server.getId())
                .name(server.getName())
                .inviteCode(server.getInvitedCode())
                .maxMembers(server.getMaxMember())
                .memberCount(serverMemberService.countServerMembers(server.getId()))
                .ownerId(server.getOwner().getId())
                .ownerName(server.getOwner().getDisplayName())
                .createdAt(server.getCreatedAt())
                .channelFolders(server.getChannelFolders())
                .build();
    }

    private ServerMemberResponse mapToMemberResponse(ServerMember member) {
        List<String> roleNames = member.getMemberRoles().stream()
                .map(mr -> mr.getRole().getName())
                .collect(Collectors.toList());

        return ServerMemberResponse.builder()
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .displayName(member.getUser().getDisplayName())
                .avatarUrl(member.getUser().getAvatarUrl())
                .joinedAt(member.getJoinedAt())
                .roles(roleNames)
                .isOwner(member.getServer().getOwner().getId().equals(member.getUser().getId()))
                .build();
    }
}
