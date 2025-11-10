package com.zvonok.repository;

import com.zvonok.model.FolderPermissionOverride;
import com.zvonok.model.ServerRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FolderPermissionOverrideRepository extends JpaRepository<FolderPermissionOverride, Long> {
    Optional<FolderPermissionOverride> findByFolderIdAndUserId(Long folderId, Long userId);

    Optional<FolderPermissionOverride> findByFolderIdAndRoleId(Long folderId, Long roleId);

    // Поиск всех переопределений ролей в папке
    @Query("""
        SELECT fpo FROM FolderPermissionOverride fpo 
        WHERE fpo.folder.id = :folderId 
        AND fpo.role IN :roles
        """)
    List<FolderPermissionOverride> findByFolderIdAndRoleIn(@Param("folderId") Long folderId,
                                                           @Param("roles") List<ServerRole> roles);

    List<FolderPermissionOverride> findByFolderId(Long folderId);

    void deleteByRoleId(Long roleId);

    void deleteByFolderIdAndUserId(Long folderId, Long userId);
}