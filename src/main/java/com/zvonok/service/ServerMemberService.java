package com.zvonok.service;

import com.zvonok.exception.ServerMemberNotFoundException;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.model.Server;
import com.zvonok.model.ServerMember;
import com.zvonok.model.User;
import com.zvonok.repository.ServerMemberRepository;
import com.zvonok.service.dto.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing server members and member-related operations.
 * Сервис для управления участниками сервера и операциями, связанными с участниками.
 */
@RequiredArgsConstructor
@Service
public class ServerMemberService {

    private final ServerMemberRepository serverMemberRepository;
    private final UserService userService;

    /** Получает участника сервера по ID. */
    public ServerMember getServerMember(Long id) {
        return serverMemberRepository.findById(id)
                .orElseThrow(() -> new ServerMemberNotFoundException(
                        HttpResponseMessage.HTTP_SERVER_MEMBER_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));
    }

    /** Получает участника сервера по ID пользователя и ID сервера. */
    public ServerMember getServerMember(Long userId, Long serverId) {
        return serverMemberRepository.findByUserIdAndServerId(userId, serverId)
                .orElseThrow(() -> new ServerMemberNotFoundException(
                        HttpResponseMessage.HTTP_SERVER_MEMBER_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));
    }

    /** Получает активного участника сервера по ID пользователя и сервера. */
    public ServerMember getActiveServerMember(Long userId, Long serverId) {
        return serverMemberRepository.findByUserIdAndServerIdAndIsActiveTrue(userId, serverId)
                .orElseThrow(() -> new ServerMemberNotFoundException(
                        HttpResponseMessage.HTTP_SERVER_MEMBER_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));
    }

    /** Обновляет участника сервера. */
    public ServerMember updateServerMember(ServerMember serverMember) {
        return serverMemberRepository.save(serverMember);
    }

    /**
     * Создает нового участника сервера для пользователя.
     * Пользователь получается по ID, участник создается с правами по умолчанию (NOTHING).
     */
    public ServerMember createServerMember(Server server, Long userId) {
        User user = userService.getUser(userId);

        ServerMember member = new ServerMember();
        member.setUser(user);
        member.setServer(server);
        member.setPersonalPermissions(Permission.NOTHING.getValue());
        member.setJoinedAt(LocalDateTime.now());

        return serverMemberRepository.save(member);
    }

    /**
     * Создает нового участника сервера для пользователя.
     * Сущность пользователя предоставляется напрямую, участник создается с правами по умолчанию (NOTHING).
     */
    public ServerMember createServerMember(Server server, User user) {
        ServerMember member = new ServerMember();
        member.setUser(user);
        member.setServer(server);
        member.setPersonalPermissions(Permission.NOTHING.getValue());
        member.setJoinedAt(LocalDateTime.now());

        return serverMemberRepository.save(member);
    }

    /** Подсчитывает количество активных участников в сервере. */
    public long countServerMembers(Long serverId) {
        return serverMemberRepository.countByServerIdAndIsActiveTrue(serverId);
    }

    /** Получает всех активных участников сервера. */
    public List<ServerMember> getAllActiveMember(Long serverId) {
        return serverMemberRepository.findByServerIdAndIsActiveTrue(serverId);
    }

    /** Обновляет ник участника сервера. */
    public ServerMember updateNickname(Long memberId, String nickname) {
        ServerMember member = getServerMember(memberId);
        member.setNickname(nickname);
        return serverMemberRepository.save(member);
    }
}
