package com.zvonok.model;

import com.zvonok.model.enumeration.MessageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.awt.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "message")
public class Message {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne @JoinColumn(name = "room_id")
    private Room room; // Для приватных и групповых сообщений

    @ManyToOne @JoinColumn(name = "channel_id")
    private Channel channel; // Для сообщений в каналах серверов

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Enumerated(EnumType.STRING)
    private MessageType type = MessageType.DEFAULT;

    private String replyToMessageId;

    private Boolean isEdited = false;
    private LocalDateTime editedAt;

    private Boolean isDeleted = false;
    private LocalDateTime deletedAt;

    @Column(nullable = false)
    private LocalDateTime sentAt;
}
