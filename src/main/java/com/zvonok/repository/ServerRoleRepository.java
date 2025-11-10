package com.zvonok.repository;

import com.zvonok.model.ServerRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServerRoleRepository extends JpaRepository<ServerRole, Long> {
    Optional<ServerRole> findByServerIdAndIsEveryoneTrue(Long serverId);
    List<ServerRole> findByServerIdAndIsActiveTrueOrderByPositionDesc(Long serverId);
    Optional<ServerRole> findByIdAndServerId(Long roleId, Long serverId);
}