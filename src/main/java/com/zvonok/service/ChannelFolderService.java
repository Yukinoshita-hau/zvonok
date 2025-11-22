package com.zvonok.service;

import com.zvonok.exception.ChannelNotFoundException;
import com.zvonok.exception.ServerNotFoundException;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.model.ChannelFolder;
import com.zvonok.model.Server;
import com.zvonok.repository.ChannelFolderRepository;
import com.zvonok.service.dto.CreateChannelFolderDto;
import com.zvonok.service.dto.UpdateChannelFolderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing channel folders within servers.
 * Сервис для управления папками каналов в серверах.
 */
@Service
public class ChannelFolderService {

    private final ChannelFolderRepository folderRepository;
    private final ServerService serverService;

    public ChannelFolderService(
            ChannelFolderRepository folderRepository,
            @Lazy ServerService serverService) {
        this.folderRepository = folderRepository;
        this.serverService = serverService;
    }

    /** Получает папку каналов по ID. */
    public ChannelFolder getChannelFolder(Long id) {
        return folderRepository.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException(
                        HttpResponseMessage.HTTP_CHANNEL_FOLDER_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));
    }

    /**
     * Создает новую папку каналов в сервере.
     * Проверяет существование сервера перед созданием папки.
     */
    public ChannelFolder createChannelFolder(CreateChannelFolderDto createChannelFolderDto) {
        // Проверяем существование сервера через ServerService
        Server server = serverService.getServer(createChannelFolderDto.getServerId());

        ChannelFolder folder = new ChannelFolder();
        folder.setName(createChannelFolderDto.getName());
        folder.setServer(server);
        folder.setPosition(createChannelFolderDto.getPosition());
        folder.setCreatedAt(LocalDateTime.now());

        return folderRepository.save(folder);
    }

    /** Получает все активные папки для сервера. */
    public List<ChannelFolder> getActiveChannelFolders(Long serverId) {
        return folderRepository.findByServerIdAndIsActiveTrueOrderByPosition(serverId);
    }

    /** Проверяет, принадлежит ли папка указанному серверу. */
    public ChannelFolder getChannelFolderForServer(Long serverId, Long folderId) {
        return folderRepository.findByIdAndServerId(folderId, serverId)
                .orElseThrow(() -> new ChannelNotFoundException(
                        HttpResponseMessage.HTTP_CHANNEL_FOLDER_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));
    }

    /** Обновляет существующую папку каналов. */
    @Transactional
    public ChannelFolder updateChannelFolder(Long folderId, UpdateChannelFolderDto updateChannelFolderDto) {
        ChannelFolder folder = getChannelFolder(folderId);

        if (updateChannelFolderDto.getName() != null) {
            folder.setName(updateChannelFolderDto.getName());
        }
        if (updateChannelFolderDto.getPosition() != null) {
            folder.setPosition(updateChannelFolderDto.getPosition());
        }
        if (updateChannelFolderDto.getCollapsed() != null) {
            folder.setCollapsed(updateChannelFolderDto.getCollapsed());
        }
        if (updateChannelFolderDto.getActive() != null) {
            folder.setIsActive(updateChannelFolderDto.getActive());
        }

        return folderRepository.save(folder);
    }

    /** Помечает папку каналов и ее каналы как неактивные. */
    @Transactional
    public void deleteChannelFolder(Long folderId) {
        ChannelFolder folder = getChannelFolder(folderId);
        folder.setIsActive(false);
        if (folder.getChannels() != null) {
            folder.getChannels().forEach(channel -> channel.setIsActive(false));
        }
        folderRepository.save(folder);
    }
}
