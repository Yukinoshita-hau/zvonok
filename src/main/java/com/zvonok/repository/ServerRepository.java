package com.zvonok.repository;

import com.zvonok.model.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServerRepository extends JpaRepository<Server, Long> {
    boolean existsByInvitedCode(String invitedCode);
    Optional<Server> findByInvitedCode(String invitedCode);
    // Серверы пользователя
    @Query("""
        SELECT s FROM Server s 
        JOIN ServerMember sm ON sm.server.id = s.id 
        WHERE sm.user.id = :userId 
        AND sm.isActive = true 
        AND s.isActive = true
        ORDER BY sm.joinedAt DESC
        """)
    List<Server> findServersByUserId(@Param("userId") Long userId);

    // Проверка владельца сервера
    @Query("""
        SELECT COUNT(s) > 0 FROM Server s 
        WHERE s.owner.id = :userId 
        AND s.id = :serverId
        """)
    boolean isServerOwner(@Param("userId") Long userId, @Param("serverId") Long serverId);
}