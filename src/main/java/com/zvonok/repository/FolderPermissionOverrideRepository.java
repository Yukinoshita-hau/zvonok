package com.zvonok.repository;

import com.zvonok.model.FolderPermissionOverride;
import com.zvonok.model.ServerRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FolderPermissionOverrideRepository extends JpaRepository<FolderPermissionOverride, Long> {
    // Поиск переопределения для конкретного пользователя в папке
    Optional<FolderPermissionOverride> findByFolderIdAndUserId(Long folderId, Long userId);

    // Поиск переопределения для конкретной роли в папке
    Optional<FolderPermissionOverride> findByFolderIdAndRoleId(Long folderId, Long roleId);

    // Поиск всех переопределений ролей в папке
    @Query("""
        SELECT fpo FROM FolderPermissionOverride fpo 
        WHERE fpo.folder.id = :folderId 
        AND fpo.role IN :roles
        """)
    List<FolderPermissionOverride> findByFolderIdAndRoleIn(@Param("folderId") Long folderId,
                                                           @Param("roles") List<ServerRole> roles);

    // Все переопределения для папки
    List<FolderPermissionOverride> findByFolderId(Long folderId);

    // Удалить все переопределения роли
    void deleteByRoleId(Long roleId);

    // Удалить все переопределения пользователя в папке
    void deleteByFolderIdAndUserId(Long folderId, Long userId);
}