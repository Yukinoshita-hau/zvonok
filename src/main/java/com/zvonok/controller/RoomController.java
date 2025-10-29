package com.zvonok.controller;

import com.zvonok.service.dto.CreateGroupDto;
import com.zvonok.model.Room;
import com.zvonok.security.dto.UserPrincipal;
import com.zvonok.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
@Slf4j
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/{id}")
    public Room getRoomByID(@PathVariable Long id) {
        return roomService.getRoom(id);
    }

    @PostMapping("/createGroup")
    public Room createGroupRoom(@RequestBody CreateGroupDto groupDto, @AuthenticationPrincipal UserPrincipal principal) {
        return roomService.createGroupRoom(principal.getName(), groupDto.getRoomName(), groupDto.getRoomMemberUsernames());
    }
}
