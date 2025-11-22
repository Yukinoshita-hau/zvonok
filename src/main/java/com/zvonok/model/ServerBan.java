package com.zvonok.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "server_ban")
public class ServerBan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "server_id")
    private Server server;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "banned_by")
    private User bannedBy;

    @Column(length = 255)
    private String reason;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private Boolean active = true;

    private LocalDateTime unbannedAt;

    @ManyToOne
    @JoinColumn(name = "unbanned_by")
    private User unbannedBy;
}

