package com.zvonok.controller;

import com.zvonok.controller.dto.MessageResponse;
import com.zvonok.service.dto.CreateGroupDto;
import com.zvonok.service.dto.UpdateRoomDto;
import com.zvonok.model.Room;
import com.zvonok.security.dto.UserPrincipal;
import com.zvonok.service.RoomService;
import com.zvonok.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final MessageService messageService;

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomByID(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoom(id));
    }

    @PostMapping("/createGroup")
    public ResponseEntity<Room> createGroupRoom(
            @Valid @RequestBody CreateGroupDto groupDto,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roomService.createGroupRoom(principal.getName(), groupDto.getRoomName(), groupDto.getRoomMemberUsernames()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoomDto roomDto,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(roomService.updateRoom(id, principal.getName(), roomDto.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        roomService.deleteRoom(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/private/{friendId}/messages")
    public ResponseEntity<List<MessageResponse>> getPrivateMessages(
            @PathVariable Long friendId,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<MessageResponse> messages = messageService.getPrivateMessages(principal.getUsername(), friendId);
        return ResponseEntity.ok(messages);
    }
}
