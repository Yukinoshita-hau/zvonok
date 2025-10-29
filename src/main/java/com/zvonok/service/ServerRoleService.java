package com.zvonok.service;

import com.zvonok.exception.ServerRoleNotFoundException;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.model.ServerRole;
import com.zvonok.repository.ServerRoleRepository;
import com.zvonok.service.dto.CreateServerRoleDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
@Slf4j
public class ServerRoleService {

    private final ServerRoleRepository serverRoleRepository;

    public ServerRole getServerRole(Long id) {
        return serverRoleRepository.findById(id).orElseThrow(() -> new ServerRoleNotFoundException(HttpResponseMessage.HTTP_SERVER_ROLE_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));
    }

    public ServerRole createServerRole(CreateServerRoleDto createServerRoleDto) {
        ServerRole everyoneRole = new ServerRole();
        everyoneRole.setName(createServerRoleDto.getName());
        everyoneRole.setColor(createServerRoleDto.getColor());
        everyoneRole.setPosition(createServerRoleDto.getPosition());
        everyoneRole.setServerPermissions(createServerRoleDto.getServerPermissions());
        everyoneRole.setIsEveryone(createServerRoleDto.isEveryone());
        everyoneRole.setMentionable(createServerRoleDto.isMentionable());
        everyoneRole.setServer(createServerRoleDto.getServer());
        everyoneRole.setCreatedAt(LocalDateTime.now());

        return serverRoleRepository.save(everyoneRole);
    }

    public ServerRole getServerRoleWithIsEveryoneTrue(Long serverId) {
        return serverRoleRepository.findByServerIdAndIsEveryoneTrue(serverId).orElseThrow(() -> new ServerRoleNotFoundException(HttpResponseMessage.HTTP_SERVER_ROLE_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));
    }
}
