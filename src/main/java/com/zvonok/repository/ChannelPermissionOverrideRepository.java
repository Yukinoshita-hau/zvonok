package com.zvonok.repository;

import com.zvonok.model.ChannelPermissionOverride;
import com.zvonok.model.ServerRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChannelPermissionOverrideRepository extends JpaRepository<ChannelPermissionOverride, Long> {
    // Поиск переопределения для конкретного пользователя в канале
    Optional<ChannelPermissionOverride> findByChannelIdAndUserId(Long channelId, Long userId);

    // Поиск переопределения для конкретной роли в канале
    Optional<ChannelPermissionOverride> findByChannelIdAndRoleId(Long channelId, Long roleId);

    // Поиск всех переопределений ролей в канале
    @Query("""
        SELECT cpo FROM ChannelPermissionOverride cpo 
        WHERE cpo.channel.id = :channelId 
        AND cpo.role IN :roles
        """)
    List<ChannelPermissionOverride> findByChannelIdAndRoleIn(@Param("channelId") Long channelId,
                                                             @Param("roles") List<ServerRole> roles);

    // Все переопределения для канала
    List<ChannelPermissionOverride> findByChannelId(Long channelId);

    // Удалить все переопределения роли
    void deleteByRoleId(Long roleId);

    // Удалить все переопределения пользователя в канале
    void deleteByChannelIdAndUserId(Long channelId, Long userId);

}