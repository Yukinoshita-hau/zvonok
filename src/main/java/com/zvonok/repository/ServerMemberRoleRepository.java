package com.zvonok.repository;

import com.zvonok.model.ServerMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServerMemberRoleRepository extends JpaRepository<ServerMemberRole, Long> {
    Optional<ServerMemberRole> findByMemberIdAndRoleId(Long memberId, Long roleId);
    List<ServerMemberRole> findByMemberId(Long memberId);
}