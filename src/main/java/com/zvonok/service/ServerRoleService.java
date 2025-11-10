package com.zvonok.service;

import com.zvonok.exception.CannotDeleteEveryoneRoleException;
import com.zvonok.exception.CannotDisableEveryoneRoleException;
import com.zvonok.exception.ServerRoleNotFoundException;
import com.zvonok.exception_handler.enumeration.BusinessRuleMessage;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.model.ServerRole;
import com.zvonok.repository.ServerRoleRepository;
import com.zvonok.service.dto.CreateServerRoleDto;
import com.zvonok.service.dto.UpdateServerRoleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing server roles and role-related operations.
 * Сервис для управления ролями сервера и операциями, связанными с ролями.
 */
@RequiredArgsConstructor
@Service
public class ServerRoleService {

    private final ServerRoleRepository serverRoleRepository;

    /** Получает роль сервера по ID. */
    public ServerRole getServerRole(Long id) {
        return serverRoleRepository.findById(id)
                .orElseThrow(() -> new ServerRoleNotFoundException(
                        HttpResponseMessage.HTTP_SERVER_ROLE_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));
    }

    /** Создает новую роль сервера. */
    public ServerRole createServerRole(CreateServerRoleDto createServerRoleDto) {
        ServerRole role = new ServerRole();
        role.setName(createServerRoleDto.getName());
        role.setColor(createServerRoleDto.getColor());
        role.setPosition(createServerRoleDto.getPosition());
        role.setServerPermissions(createServerRoleDto.getServerPermissions());
        role.setIsEveryone(createServerRoleDto.isEveryone());
        role.setMentionable(createServerRoleDto.isMentionable());
        role.setServer(createServerRoleDto.getServer());
        role.setCreatedAt(LocalDateTime.now());

        return serverRoleRepository.save(role);
    }

    /**
     * Получает роль "everyone" по умолчанию для сервера.
     * Роль "everyone" назначается всем новым участникам по умолчанию.
     */
    public ServerRole getServerRoleWithIsEveryoneTrue(Long serverId) {
        return serverRoleRepository.findByServerIdAndIsEveryoneTrue(serverId)
                .orElseThrow(() -> new ServerRoleNotFoundException(
                        HttpResponseMessage.HTTP_SERVER_ROLE_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));
    }

    /** Получает все активные роли сервера. */
    public List<ServerRole> getActiveServerRoles(Long serverId) {
        return serverRoleRepository.findByServerIdAndIsActiveTrueOrderByPositionDesc(serverId);
    }

    /** Получает роль по ID, проверяя принадлежность серверу. */
    public ServerRole getServerRoleForServer(Long serverId, Long roleId) {
        return serverRoleRepository.findByIdAndServerId(roleId, serverId)
                .orElseThrow(() -> new ServerRoleNotFoundException(
                        HttpResponseMessage.HTTP_SERVER_ROLE_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));
    }

    /** Обновляет существующую роль сервера. */
    @Transactional
    public ServerRole updateServerRole(Long roleId, UpdateServerRoleDto updateServerRoleDto) {
        ServerRole role = getServerRole(roleId);

        if (updateServerRoleDto.getName() != null) {
            role.setName(updateServerRoleDto.getName());
        }
        if (updateServerRoleDto.getColor() != null) {
            role.setColor(updateServerRoleDto.getColor());
        }
        if (updateServerRoleDto.getPosition() != null) {
            role.setPosition(updateServerRoleDto.getPosition());
        }
        if (updateServerRoleDto.getServerPermissions() != null) {
            role.setServerPermissions(updateServerRoleDto.getServerPermissions());
        }
        if (updateServerRoleDto.getMentionable() != null) {
            role.setMentionable(updateServerRoleDto.getMentionable());
        }
        if (updateServerRoleDto.getActive() != null) {
            if (Boolean.FALSE.equals(updateServerRoleDto.getActive()) && role.getIsEveryone()) {
                throw new CannotDisableEveryoneRoleException(
                        BusinessRuleMessage.BUSINESS_CANNOT_DISABLE_EVERYONE_ROLE_MESSAGE.getMessage());
            }
            role.setIsActive(updateServerRoleDto.getActive());
        }

        return serverRoleRepository.save(role);
    }

    /** Помечает роль как неактивную. */
    @Transactional
    public void deleteServerRole(Long roleId) {
        ServerRole role = getServerRole(roleId);
        if (role.getIsEveryone()) {
            throw new CannotDeleteEveryoneRoleException(
                    BusinessRuleMessage.BUSINESS_CANNOT_DELETE_EVERYONE_ROLE_MESSAGE.getMessage());
        }
        role.setIsActive(false);
        serverRoleRepository.save(role);
    }
}
