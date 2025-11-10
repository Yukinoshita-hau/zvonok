package com.zvonok.model;

import com.zvonok.exception.PermissionOverrideTargetMissingException;
import com.zvonok.exception_handler.enumeration.BusinessRuleMessage;
import com.zvonok.service.dto.Permission;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "channel_permission_override")
public class ChannelPermissionOverride {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @ManyToOne @JoinColumn(name = "role_id")
    private ServerRole role;

    @ManyToOne @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Long allowedPermissions = Permission.NOTHING.getValue();

    @Column(nullable = false)
    private Long deniedPermissions = Permission.NOTHING.getValue();

    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    private void validateOverride() {
        // Должен быть либо role, либо user, но не оба
        if ((role == null && user == null) || (role != null && user != null)) {
            throw new PermissionOverrideTargetMissingException(
                    BusinessRuleMessage.BUSINESS_PERMISSION_OVERRIDE_TARGET_REQUIRED_MESSAGE.getMessage());
        }
    }
}
