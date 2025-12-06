package com.zvonok.service;

import com.zvonok.exception.ServerBanNotFoundException;
import com.zvonok.exception.ServerMemberNotFoundException;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.model.Server;
import com.zvonok.model.ServerBan;
import com.zvonok.model.ServerMember;
import com.zvonok.model.User;
import com.zvonok.repository.ServerBanRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServerBanService {

    private final ServerBanRepository serverBanRepository;
    private final ServerService serverService;
    private final UserService userService;
    private final ServerMemberService serverMemberService;

    public ServerBanService(
            ServerBanRepository serverBanRepository,
            @Lazy ServerService serverService,
            UserService userService,
            ServerMemberService serverMemberService) {
        this.serverBanRepository = serverBanRepository;
        this.serverService = serverService;
        this.userService = userService;
        this.serverMemberService = serverMemberService;
    }

    @Transactional
    public boolean isUserBanned(Long serverId, Long userId) {
        Optional<ServerBan> optionalBan = serverBanRepository.findByServerIdAndUserIdAndActiveTrue(serverId, userId);
        if (optionalBan.isEmpty()) {
            return false;
        }

        ServerBan ban = optionalBan.get();
        if (ban.getExpiresAt() != null && ban.getExpiresAt().isBefore(LocalDateTime.now())) {
            // бан истек - снимаем
            deactivateBan(ban, null);
            return false;
        }

        return true;
    }

    @Transactional
    public List<ServerBan> getActiveBans(Long serverId) {
        List<ServerBan> bans = serverBanRepository.findByServerIdAndActiveTrue(serverId);
        return bans.stream()
                .filter(ban -> {
                    if (ban.getExpiresAt() != null && ban.getExpiresAt().isBefore(LocalDateTime.now())) {
                        deactivateBan(ban, null);
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ServerBan banUser(Long serverId, Long targetUserId, Long actorUserId, String reason, LocalDateTime expiresAt) {
        Server server = serverService.getServer(serverId);
        User targetUser = userService.getUser(targetUserId);
        User actor = userService.getUser(actorUserId);

        Optional<ServerBan> existingBanOpt = serverBanRepository.findByServerIdAndUserIdAndActiveTrue(serverId, targetUserId);
        ServerBan ban = existingBanOpt.orElseGet(ServerBan::new);

        ban.setServer(server);
        ban.setUser(targetUser);
        ban.setBannedBy(actor);
        ban.setReason(reason);
        ban.setCreatedAt(LocalDateTime.now());
        ban.setExpiresAt(expiresAt);
        ban.setActive(true);
        ban.setUnbannedAt(null);
        ban.setUnbannedBy(null);

        ServerBan savedBan = serverBanRepository.save(ban);

        // деактивируем участника, если он есть
        try {
            ServerMember member = serverMemberService.getServerMember(targetUserId, serverId);
            member.setIsActive(false);
            member.setLeftAt(LocalDateTime.now());
            serverMemberService.updateServerMember(member);
        } catch (ServerMemberNotFoundException ignored) {
            // Пользователь мог не быть участником - пропускаем
        }

        return savedBan;
    }

    @Transactional
    public void unbanUser(Long serverId, Long targetUserId, Long actorUserId) {
        ServerBan ban = serverBanRepository.findByServerIdAndUserIdAndActiveTrue(serverId, targetUserId)
                .orElseThrow(() -> new ServerBanNotFoundException(
                        HttpResponseMessage.HTTP_SERVER_BAN_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));

        User actor = userService.getUser(actorUserId);
        deactivateBan(ban, actor);
    }

    private void deactivateBan(ServerBan ban, User unbannedBy) {
        ban.setActive(false);
        ban.setUnbannedAt(LocalDateTime.now());
        ban.setUnbannedBy(unbannedBy);
        serverBanRepository.save(ban);
    }
}

