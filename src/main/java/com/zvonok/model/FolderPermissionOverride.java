package com.zvonok.model;

import com.zvonok.exception.RedefinitionException;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.service.dto.Permission;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "folder_permission_override")
public class FolderPermissionOverride {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "folder_id", nullable = false)
    private ChannelFolder folder;

    @ManyToOne @JoinColumn(name = "role_id")
    private ServerRole role;

    @ManyToOne @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Long allowedPermissions = Permission.NOTHING.getValue(); // Что разрешено в этой папке

    @Column(nullable = false)
    private Long deniedPermissions = Permission.NOTHING.getValue(); // Что запрещено в этой папке

    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    private void validateOverride() {
        if ((role == null && user == null) || (role != null && user != null)) {
            throw new RedefinitionException(HttpResponseMessage.HTTP_REDEFINITION_RESPONSE_MESSAGE.getMessage());
        }
    }
}
