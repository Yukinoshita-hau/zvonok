package com.zvonok.model;

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
@Table(name = "server_member_role")
public class ServerMemberRole {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "member_id", nullable = false)
    private ServerMember member;

    @ManyToOne @JoinColumn(name = "role_id", nullable = false)
    private ServerRole role;

    private LocalDateTime assignedAt;

    @ManyToOne @JoinColumn(name = "assigned_by", nullable = false)
    private User assignedBy;
}
