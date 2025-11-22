package com.zvonok.service;

import com.zvonok.exception.*;
import com.zvonok.exception.UserBannedException;
import com.zvonok.exception_handler.enumeration.BusinessRuleMessage;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing servers, server members, and server-related operations.
 * Сервис для управления серверами, участниками серверов и операциями, связанными с серверами.
 */
@Service
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
    private final ServerBanService serverBanService;

    public ServerService(
            ServerRepository serverRepository,
            UserService userService,
            InviteCodeService inviteCodeService,
            PermissionService permissionService,
            ServerMemberService serverMemberService,
            ServerMemberRoleService serverMemberRoleService,
            ServerRoleService serverRoleService,
            @Lazy ChannelService channelService,
            @Lazy ChannelFolderService channelFolderService,
            @Lazy ServerBanService serverBanService) {
        this.serverRepository = serverRepository;
        this.userService = userService;
        this.inviteCodeService = inviteCodeService;
        this.permissionService = permissionService;
        this.serverMemberService = serverMemberService;
        this.serverMemberRoleService = serverMemberRoleService;
        this.serverRoleService = serverRoleService;
        this.channelService = channelService;
        this.channelFolderService = channelFolderService;
        this.serverBanService = serverBanService;
    }

    /**
     * Creates a new server with default roles, channels, and adds the owner as a member.
     * Creates default "everyone" and "owner" roles, adds owner as a member with owner role,
     * and creates default channel folder with text and voice channels.
     *
     * Создает новый сервер с ролями по умолчанию, каналами и добавляет владельца в качестве участника.
     * Создает роли "everyone" и "owner" по умолчанию, добавляет владельца как участника с ролью владельца,
     * и создает папку каналов по умолчанию с текстовым и голосовым каналами.
     *
     * @param request  the request containing server creation data
     *                 запрос, содержащий данные для создания сервера
     * @param ownerId  the ID of the user who will be the server owner
     *                 идентификатор пользователя, который будет владельцем сервера
     * @return ServerResponse containing the created server information
     *         ответ, содержащий информацию о созданном сервере
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
        createEveryoneRole(savedServer);
        ServerRole ownerRole = createOwnerRole(savedServer);

        // Добавляем владельца как участника
        ServerMember ownerMember = addOwnerAsMember(savedServer, owner);
        assignRoleToMember(ownerMember, ownerRole, ownerId);

        // Создаем дефолтную папку и каналы
        ChannelFolder defaultFolder = createDefaultFolder(savedServer.getId());
        createDefaultChannels(defaultFolder.getId());

        return mapToResponse(savedServer);
    }

    /** Получает сервер по ID. */
    public Server getServer(Long serverId) {
        return serverRepository.findById(serverId)
                .orElseThrow(() -> new ServerNotFoundException("Сервер не найден с ID: " + serverId));
    }

    /** Получает все серверы, участником которых является пользователь. */
    public List<ServerResponse> getUserServers(Long userId) {
        List<Server> servers = serverRepository.findServersByUserId(userId);
        return servers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /** Проверяет, имеет ли пользователь доступ к серверу (является ли участником). */
    public boolean hasAccessToServer(Long userId, Long serverId) {
        return permissionService.isServerMember(userId, serverId);
    }

    /**
     * Проверяет доступ пользователя к серверу и выбрасывает исключение, если нет.
     * Используется для проверки контроля доступа.
     */
    public void hasAccessToServerAndThrowExceptionIfFalse(Long userId, Long serverId) {
        if (!hasAccessToServer(userId, serverId)) {
            throw new InsufficientPermissionsException(
                    HttpResponseMessage.HTTP_INSUFFICIENT_PERMISSIONS_RESPONSE_MESSAGE.getMessage());
        }
    }

    /** Получает DTO ответа сервера по ID сервера. */
    public ServerResponse getServerResponse(Long serverId) {
        Server server = getServer(serverId);
        return mapToResponse(server);
    }

    /**
     * Allows a user to join a server using an invite code.
     * Validates the invite code, checks server status, member limit, and adds the user as a member
     * with the default "everyone" role. If the user is already a member, returns the server response.
     *
     * Позволяет пользователю присоединиться к серверу, используя код приглашения.
     * Проверяет код приглашения, статус сервера, лимит участников и добавляет пользователя как участника
     * с ролью "everyone" по умолчанию. Если пользователь уже является участником, возвращает ответ сервера.
     *
     * @param inviteCode  the invite code for the server
     *                    код приглашения для сервера
     * @param userId      the unique identifier of the user joining the server
     *                    уникальный идентификатор пользователя, присоединяющегося к серверу
     * @return ServerResponse containing the server information
     *         ответ, содержащий информацию о сервере
     * @throws ServerNotFoundException  if server with the invite code does not exist or is not active
     *                                  если сервер с кодом приглашения не существует или не активен
     * @throws ServerMemberLimitReachedException if the server has reached the maximum member limit
     *                                           если сервер достиг максимального лимита участников
     */
    @Transactional
    public ServerResponse joinServerByInviteCode(String inviteCode, Long userId) {
        Server server = serverRepository.findByInvitedCode(inviteCode)
                .orElseThrow(() -> new ServerNotFoundException(
                        HttpResponseMessage.HTTP_SERVER_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));

        if (!server.getIsActive()) {
            throw new ServerNotFoundException(
                    HttpResponseMessage.HTTP_SERVER_NOT_ACTIVE_RESPONSE_MESSAGE.getMessage());
        }

        User user = userService.getUser(userId);

        if (serverBanService.isUserBanned(server.getId(), userId)) {
            throw new UserBannedException(
                    HttpResponseMessage.HTTP_USER_BANNED_RESPONSE_MESSAGE.getMessage());
        }

        // Проверяем не является ли уже участником
        if (isServerMember(userId, server)) {
            return mapToResponse(server); // Уже участник
        }

        // Проверяем лимит участников
        long memberCount = serverMemberService.countServerMembers(server.getId());
        if (memberCount >= server.getMaxMember()) {
            throw new ServerMemberLimitReachedException(
                    BusinessRuleMessage.BUSINESS_SERVER_MEMBER_LIMIT_REACHED_MESSAGE.getMessage());
        }

        // Добавляем как участника
        ServerMember newMember = addUserAsMember(server, user);

        // Назначаем роль @everyone
        ServerRole everyoneRole = serverRoleService.getServerRoleWithIsEveryoneTrue(server.getId());
        assignRoleToMember(newMember, everyoneRole, userId);

        return mapToResponse(server);
    }

    /** Проверяет, является ли пользователь участником указанного сервера. */
    public boolean isServerMember(Long userId, Server server) {
        return serverMemberService.getServerMember(userId, server.getId()) != null;
    }

    /**
     * Updates server information such as name and maximum member count.
     * Requires MANAGE_SERVER permission or server ownership.
     *
     * Обновляет информацию о сервере, такую как название и максимальное количество участников.
     * Требует права MANAGE_SERVER или владения сервером.
     *
     * @param serverId  the unique identifier of the server to update
     *                  уникальный идентификатор сервера для обновления
     * @param request   the request containing updated server data (name, maxMembers)
     *                  запрос, содержащий обновленные данные сервера (название, максимальное количество участников)
     * @param userId    the unique identifier of the user performing the update
     *                  уникальный идентификатор пользователя, выполняющего обновление
     * @return ServerResponse containing the updated server information
     *         ответ, содержащий обновленную информацию о сервере
     * @throws ServerNotFoundException        if server with the given ID does not exist
     *                                        если сервер с указанным идентификатором не существует
     * @throws InsufficientPermissionsException if user lacks permission to manage the server
     *                                          если у пользователя недостаточно прав для управления сервером
     */
    @Transactional
    public ServerResponse updateServer(Long serverId, UpdateServerRequest request, Long userId) {
        Server server = getServer(serverId);

        // Проверяем права на управление сервером
        if (!canManageServer(userId, serverId)) {
            throw new InsufficientPermissionsException(
                    HttpResponseMessage.HTTP_INSUFFICIENT_PERMISSIONS_RESPONSE_MESSAGE.getMessage());
        }

        if (request.getName() != null) {
            server.setName(request.getName());
        }

        if (request.getMaxMembers() != null) {
            server.setMaxMember(request.getMaxMembers());
        }

        Server updatedServer = serverRepository.save(server);
        return mapToResponse(updatedServer);
    }

    /**
     * Regenerates the invite code for a server.
     * Generates a new unique invite code and replaces the old one.
     * Requires MANAGE_SERVER permission or server ownership.
     *
     * Регенерирует код приглашения для сервера.
     * Генерирует новый уникальный код приглашения и заменяет старый.
     * Требует права MANAGE_SERVER или владения сервером.
     *
     * @param serverId  the unique identifier of the server
     *                  уникальный идентификатор сервера
     * @param userId    the unique identifier of the user performing the regeneration
     *                  уникальный идентификатор пользователя, выполняющего регенерацию
     * @return the newly generated invite code
     *         новый сгенерированный код приглашения
     * @throws ServerNotFoundException        if server with the given ID does not exist
     *                                        если сервер с указанным идентификатором не существует
     * @throws InsufficientPermissionsException if user lacks permission to manage the server
     *                                          если у пользователя недостаточно прав для управления сервером
     */
    @Transactional
    public String regenerateInviteCode(Long serverId, Long userId) {
        Server server = getServer(serverId);

        if (!canManageServer(userId, serverId)) {
            throw new InsufficientPermissionsException(
                    HttpResponseMessage.HTTP_INSUFFICIENT_PERMISSIONS_RESPONSE_MESSAGE.getMessage());
        }

        String newInviteCode = inviteCodeService.generateUniqueInviteCode();
        server.setInvitedCode(newInviteCode);
        serverRepository.save(server);

        return newInviteCode;
    }

    /**
     * Allows a user to leave a server.
     * Marks the server member as inactive and sets the leave timestamp.
     * Server owners cannot leave their own servers.
     *
     * Позволяет пользователю покинуть сервер.
     * Помечает участника сервера как неактивного и устанавливает время выхода.
     * Владельцы серверов не могут покинуть свои серверы.
     *
     * @param serverId  the unique identifier of the server to leave
     *                  уникальный идентификатор сервера, который нужно покинуть
     * @param userId    the unique identifier of the user leaving the server
     *                  уникальный идентификатор пользователя, покидающего сервер
     * @throws ServerNotFoundException        if server with the given ID does not exist
     *                                        если сервер с указанным идентификатором не существует
     * @throws OwnerCanNotLeaveServerException if the user is the server owner
     *                                         если пользователь является владельцем сервера
     */
    @Transactional
    public void leaveServer(Long serverId, Long userId) {
        Server server = getServer(serverId);

        // Владелец не может покинуть сервер
        if (server.getOwner().getId().equals(userId)) {
            throw new OwnerCanNotLeaveServerException(
                    HttpResponseMessage.HTTP_OWNER_CAN_NOT_LEAVE_SERVER_RESPONSE_MESSAGE.getMessage());
        }

        ServerMember member = serverMemberService.getServerMember(userId, serverId);

        member.setIsActive(false);
        member.setLeftAt(LocalDateTime.now());
        serverMemberService.updateServerMember(member);

    }

    /**
     * Retrieves all active members of a server.
     * Only server members can view the member list.
     *
     * Получает всех активных участников сервера.
     * Только участники сервера могут просматривать список участников.
     *
     * @param serverId  the unique identifier of the server
     *                  уникальный идентификатор сервера
     * @param userId    the unique identifier of the user requesting the member list
     *                  уникальный идентификатор пользователя, запрашивающего список участников
     * @return List of ServerMemberResponse objects containing member information
     *         список объектов ServerMemberResponse, содержащих информацию об участниках
     * @throws ServerNotFoundException        if server with the given ID does not exist
     *                                        если сервер с указанным идентификатором не существует
     * @throws InsufficientPermissionsException if user is not a server member
     *                                          если пользователь не является участником сервера
     */
    public List<ServerMemberResponse> getServerMembers(Long serverId, Long userId) {
        // Проверяем может ли пользователь видеть участников
        if (!canViewMembers(userId, serverId)) {
            throw new InsufficientPermissionsException(
                    HttpResponseMessage.HTTP_INSUFFICIENT_PERMISSIONS_RESPONSE_MESSAGE.getMessage());
        }

        List<ServerMember> members = serverMemberService.getAllActiveMember(serverId);
        return members.stream()
                .map(this::mapToMemberResponse)
                .collect(Collectors.toList());
    }

    /**
     * Kicks a member from the server.
     * Marks the member as inactive and sets the leave timestamp.
     * Requires KICK_MEMBERS permission or server ownership.
     * Cannot kick the server owner or yourself.
     *
     * Исключает участника из сервера.
     * Помечает участника как неактивного и устанавливает время выхода.
     * Требует права KICK_MEMBERS или владения сервером.
     * Нельзя исключить владельца сервера или самого себя.
     *
     * @param serverId    the unique identifier of the server
     *                    уникальный идентификатор сервера
     * @param targetUserId the unique identifier of the user to be kicked
     *                     уникальный идентификатор пользователя, которого нужно исключить
     * @param kickerUserId the unique identifier of the user performing the kick
     *                     уникальный идентификатор пользователя, выполняющего исключение
     * @throws ServerNotFoundException        if server with the given ID does not exist
     *                                        если сервер с указанным идентификатором не существует
     * @throws InsufficientPermissionsException if kicker lacks permission to kick members
     *                                          если у исключающего недостаточно прав для исключения участников
     * @throws CannotKickServerOwnerException if attempting to kick the server owner
     *                                        если попытка исключить владельца сервера
     * @throws CannotKickYourselfException    if attempting to kick yourself
     *                                        если попытка исключить самого себя
     */
    @Transactional
    public void kickMember(Long serverId, Long targetUserId, Long kickerUserId) {
        Server server = getServer(serverId);

        // Проверяем права на исключение
        if (!canKickMembers(kickerUserId, serverId)) {
            throw new InsufficientPermissionsException(
                    HttpResponseMessage.HTTP_INSUFFICIENT_PERMISSIONS_RESPONSE_MESSAGE.getMessage());
        }

        // Нельзя исключить владельца
        if (server.getOwner().getId().equals(targetUserId)) {
            throw new CannotKickServerOwnerException(
                    BusinessRuleMessage.BUSINESS_CANNOT_KICK_SERVER_OWNER_MESSAGE.getMessage());
        }

        // Нельзя исключить самого себя
        if (kickerUserId.equals(targetUserId)) {
            throw new CannotKickYourselfException(
                    BusinessRuleMessage.BUSINESS_CANNOT_KICK_SELF_MESSAGE.getMessage());
        }

        ServerMember targetMember = serverMemberService.getServerMember(targetUserId, serverId);

        targetMember.setIsActive(false);
        targetMember.setLeftAt(LocalDateTime.now());
        serverMemberService.updateServerMember(targetMember);

    }

    /**
     * Обновляет ник участника сервера.
     * Участник может изменить свой собственный ник, а также это могут делать пользователи с правами управления сервером.
     */
    @Transactional
    public ServerMemberResponse updateMemberNickname(Long serverId, Long targetUserId, String nickname, Long actorUserId) {
        // Убедимся, что сервер существует
        getServer(serverId);

        // Получаем участников
        ServerMember actorMember = serverMemberService.getServerMember(actorUserId, serverId);
        ServerMember targetMember = serverMemberService.getServerMember(targetUserId, serverId);

        boolean isSelfUpdate = targetMember.getUser().getId().equals(actorMember.getUser().getId());
        if (!isSelfUpdate && !permissionService.hasPermissionInServer(actorUserId, serverId, Permission.MANAGE_SERVER)) {
            throw new InsufficientPermissionsException(
                    HttpResponseMessage.HTTP_INSUFFICIENT_PERMISSIONS_RESPONSE_MESSAGE.getMessage());
        }

        String processedNickname = StringUtils.hasText(nickname) ? nickname.trim() : null;
        targetMember.setNickname(processedNickname);
        serverMemberService.updateServerMember(targetMember);

        return mapToMemberResponse(targetMember);
    }

    // ===== PRIVATE HELPER METHODS =====

    private ServerRole createEveryoneRole(Server server) {
        CreateServerRoleDto createDto = new CreateServerRoleDto();
        createDto.setName("everyone");
        createDto.setColor("#ffffff");
        createDto.setPosition(0);
        createDto.setServerPermissions(
                Permission.VIEW_CHANNEL.getValue() |
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
        serverMemberRoleService.createServerMemberRole(member, role, assignedById);
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

    // ===== PERMISSION CHECKS =====

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

    // ===== MAPPING METHODS =====

    /** Преобразует сущность Server в DTO ServerResponse. */
    public ServerResponse mapToResponse(Server server) {
        return ServerResponse.builder()
                .id(server.getId())
                .name(server.getName())
                .inviteCode(server.getInvitedCode())
                .maxMembers(server.getMaxMember())
                .memberCount(serverMemberService.countServerMembers(server.getId()))
                .ownerId(server.getOwner().getId())
                .ownerName(server.getOwner().getUsername())
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
                .avatarUrl(member.getUser().getAvatarUrl())
                .nickname(member.getNickname())
                .joinedAt(member.getJoinedAt())
                .roles(roleNames)
                .isOwner(member.getServer().getOwner().getId().equals(member.getUser().getId()))
                .build();
    }

    /**
     * Удаляет сервер. Только владелец может удалить сервер.
     * Deletes a server. Only the owner can delete the server.
     */
    @Transactional
    public void deleteServer(Long serverId, Long userId) {
        Server server = getServer(serverId);
        
        // Проверяем, что пользователь является владельцем
        if (!server.getOwner().getId().equals(userId)) {
            throw new InsufficientPermissionsException(
                    HttpResponseMessage.HTTP_INSUFFICIENT_PERMISSIONS_RESPONSE_MESSAGE.getMessage());
        }
        
        // Удаляем сервер (каскадное удаление должно быть настроено в JPA)
        serverRepository.delete(server);
    }
}
