package com.zvonok.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zvonok.model.enumeration.RoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "room")
public class Room {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    private RoomType type = RoomType.PRIVATE;

    private Boolean isActive = true;

    private LocalDateTime createdAt;

    @ManyToMany @JoinTable(
            name = "room_members",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private List<User> members;
}
