package com.zvonok.repository;

import com.zvonok.model.ChannelFolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChannelFolderRepository extends JpaRepository<ChannelFolder, Long> {
    List<ChannelFolder> findByServerIdAndIsActiveTrueOrderByPosition(Long serverId);
    Optional<ChannelFolder> findByIdAndServerId(Long folderId, Long serverId);
}