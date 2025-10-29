package com.zvonok.service;

import com.zvonok.exception.ChannelNotFoundException;
import com.zvonok.exception.ServerNotFoundException;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.model.ChannelFolder;
import com.zvonok.model.Server;
import com.zvonok.repository.ChannelFolderRepository;
import com.zvonok.repository.ServerRepository;
import com.zvonok.service.dto.CreateChannelFolderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChannelFolderService {

    private final ChannelFolderRepository folderRepository;
    private final ServerRepository serverRepository;

    public ChannelFolder getChannelFolder(Long id) {
        return folderRepository.findById(id).orElseThrow(() -> new ChannelNotFoundException(HttpResponseMessage.HTTP_CHANNEL_FOLDER_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));
    }

    public ChannelFolder createChannelFolder(CreateChannelFolderDto createChannelFolderDto) {
        Server server = serverRepository.findById(createChannelFolderDto.getServerId()).orElseThrow(() -> new ServerNotFoundException(HttpResponseMessage.HTTP_SERVER_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));

        ChannelFolder folder = new ChannelFolder();
        folder.setName(createChannelFolderDto.getName());
        folder.setServer(server);
        folder.setPosition(createChannelFolderDto.getPosition());
        folder.setCreatedAt(LocalDateTime.now());

        return folderRepository.save(folder);
    };
}
