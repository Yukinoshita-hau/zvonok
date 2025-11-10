package com.zvonok.repository;

import com.zvonok.model.ServerBan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServerBanRepository extends JpaRepository<ServerBan, Long> {
    Optional<ServerBan> findByServerIdAndUserIdAndActiveTrue(Long serverId, Long userId);
    List<ServerBan> findByServerIdAndActiveTrue(Long serverId);
}

