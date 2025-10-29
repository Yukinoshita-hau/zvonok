package com.zvonok.service;

import com.zvonok.exception.ChannelNotFoundException;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.model.Channel;
import com.zvonok.model.ChannelFolder;
import com.zvonok.model.enumeration.ChannelType;
import com.zvonok.repository.ChannelFolderRepository;
import com.zvonok.repository.ChannelRepository;
import com.zvonok.service.dto.CreateChannelDto;
import com.zvonok.service.dto.CreateChannelFolderDto;
import com.zvonok.service.dto.CreateServerRoleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelFolderRepository folderRepository;

    public Channel getChannel(long id) {
        return channelRepository.findById(id).orElseThrow(() -> new ChannelNotFoundException(HttpResponseMessage.HTTP_CHANNEL_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));
    }

    public List<Channel> getChannelsByFolderId(Long folderId) {
        return channelRepository.findByFolderIdAndIsActiveTrue(folderId);
    }

    public Channel createChannel(CreateChannelDto createChannelFolderDto) {
        ChannelFolder folder = folderRepository.findById(createChannelFolderDto.getFolderId()).orElseThrow(() -> new ChannelNotFoundException(HttpResponseMessage.HTTP_CHANNEL_FOLDER_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));

        Channel generalChannel = new Channel();
        generalChannel.setName(createChannelFolderDto.getName());
        generalChannel.setFolder(folder);
        generalChannel.setType(createChannelFolderDto.getType());
        generalChannel.setPosition(createChannelFolderDto.getPosition());
        generalChannel.setTopic(createChannelFolderDto.getTopic());
        generalChannel.setCreatedAt(LocalDateTime.now());

        return channelRepository.save(generalChannel);
    }
}
