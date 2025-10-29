package com.zvonok.service;

import com.zvonok.exception.RoomNotFoundException;
import com.zvonok.exception.RoomSizeMaxTenMembersException;
import com.zvonok.exception.UserNotFoundException;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.model.Room;
import com.zvonok.model.User;
import com.zvonok.model.enumeration.RoomType;
import com.zvonok.repository.RoomRepository;
import com.zvonok.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public Room getRoom(Long id) {
        Optional<Room> optionalRoom = roomRepository.findById(id);

        if (optionalRoom.isPresent()) {
            return optionalRoom.get();
        } else {
            throw new RoomNotFoundException(HttpResponseMessage.HTTP_ROOM_NOT_FOUND_RESPONSE_MESSAGE.getMessage());
        }
    }

    public Room createOrGetPrivateRoom(String username1, String username2) {
        User user1 = userRepository.findByUsername(username1)
                .orElseThrow(() -> new UserNotFoundException(HttpResponseMessage.HTTP_USER_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));

        User user2 = userRepository.findByUsername(username2)
                .orElseThrow(() -> new UserNotFoundException(HttpResponseMessage.HTTP_USER_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));

        Optional<Room> existingRoom = findPrivateRoomBetweenUsers(user1.getId(), user2.getId());
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        Room room = new Room();
        // у приватных рум небудет названия
        room.setName(null);
        room.setType(RoomType.PRIVATE);
        room.setIsActive(true);
        room.setCreateAt(LocalDateTime.now());
        room.setMembers(Arrays.asList(user1, user2));

        return roomRepository.save(room);
    }

    public Room createGroupRoom(String creatorUsername,
                            String roomName,
                            List<String> roomMemberUsernames) {

        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new UserNotFoundException(HttpResponseMessage.HTTP_USER_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));

        List<User> members = userRepository.findAllByUsernameIn(roomMemberUsernames);

        if (!members.contains(creator)) {
            members.add(creator);
        }

        if (members.size() > 10) {
            throw new RoomSizeMaxTenMembersException(HttpResponseMessage.HTTP_ROOM_SIZE_MAX_TEN_MEMBERS_RESPONSE_MESSAGE.getMessage());
        }

        Room room = new Room();
        room.setName(roomName);
        room.setType(RoomType.GROUP);
        room.setIsActive(true);
        room.setCreateAt(LocalDateTime.now());
        room.setMembers(members);

        return roomRepository.save(room);
    }

    private Optional<Room> findPrivateRoomBetweenUsers(Long user1, Long user2) {
        return roomRepository.findPrivateRoomBetweenUsers(user1, user2);
    }

    public List<Room> getUserRooms(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(HttpResponseMessage.HTTP_USER_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));

        return roomRepository.findAllByMembersContainingAndIsActiveTrue(user);
    }

    public void leaveRoom(String username, long roomId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(HttpResponseMessage.HTTP_USER_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));

        Room room = getRoom(roomId);

        room.getMembers().remove(user);

        if (room.getMembers().isEmpty()) {
            room.setIsActive(false);
        }

        roomRepository.save(room);
    }
}
