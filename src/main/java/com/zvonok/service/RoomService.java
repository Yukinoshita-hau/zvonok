package com.zvonok.service;

import com.zvonok.exception.InsufficientPermissionsException;
import com.zvonok.exception.RoomNotFoundException;
import com.zvonok.exception.RoomSizeMaxTenMembersException;
import com.zvonok.exception.UserNotFoundException;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.model.Room;
import com.zvonok.model.User;
import com.zvonok.model.enumeration.RoomType;
import com.zvonok.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing private and group chat rooms.
 * Сервис для управления приватными и групповыми чат-комнатами.
 */
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserService userService;

    /** Получает комнату по ID. */
    public Room getRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(
                        HttpResponseMessage.HTTP_ROOM_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));
    }

    /**
     * Creates or retrieves an existing private room between two users.
     * If a private room already exists between the users, it is returned.
     * Otherwise, a new private room is created.
     *
     * Создает или получает существующую приватную комнату между двумя пользователями.
     * Если приватная комната уже существует между пользователями, она возвращается.
     * В противном случае создается новая приватная комната.
     *
     * @param username1  the username of the first user
     *                   имя пользователя первого пользователя
     * @param username2  the username of the second user
     *                   имя пользователя второго пользователя
     * @return Room entity (existing or newly created)
     *         сущность Room (существующая или вновь созданная)
     * @throws UserNotFoundException if either user does not exist
     *                               если один из пользователей не существует
     */
    public Room createOrGetPrivateRoom(String username1, String username2) {
        User user1 = userService.getUser(username1);
        User user2 = userService.getUser(username2);

        Optional<Room> existingRoom = findPrivateRoomBetweenUsers(user1.getId(), user2.getId());
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        Room room = new Room();
        // У приватных комнат нет названия (name = null), так как это приватная комната
        room.setName(null);
        room.setType(RoomType.PRIVATE);
        room.setIsActive(true);
        room.setCreateAt(LocalDateTime.now());
        room.setMembers(Arrays.asList(user1, user2));

        return roomRepository.save(room);
    }

    /**
     * Creates a new group room with specified members.
     * The creator is automatically added to the room if not already in the member list.
     * Group rooms can have a maximum of 10 members.
     *
     * Создает новую групповую комнату с указанными участниками.
     * Создатель автоматически добавляется в комнату, если его еще нет в списке участников.
     * Групповые комнаты могут содержать максимум 10 участников.
     *
     * @param creatorUsername      the username of the user creating the room
     *                             имя пользователя, создающего комнату
     * @param roomName             the name of the group room
     *                             название групповой комнаты
     * @param roomMemberUsernames  list of usernames to add as members
     *                             список имен пользователей для добавления в качестве участников
     * @return the created Room entity
     *         созданная сущность Room
     * @throws UserNotFoundException         if creator or any member does not exist
     *                                       если создатель или любой участник не существует
     * @throws RoomSizeMaxTenMembersException if the number of members exceeds 10
     *                                        если количество участников превышает 10
     */
    public Room createGroupRoom(String creatorUsername, String roomName, List<String> roomMemberUsernames) {
        User creator = userService.getUser(creatorUsername);
        
        // Получаем пользователей по именам через UserService
        List<User> members = roomMemberUsernames.stream()
                .map(userService::getUser)
                .toList();

        if (!members.contains(creator)) {
            members.add(creator);
        }

        if (members.size() > 10) {
            throw new RoomSizeMaxTenMembersException(
                    HttpResponseMessage.HTTP_ROOM_SIZE_MAX_TEN_MEMBERS_RESPONSE_MESSAGE.getMessage());
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

    /** Получает все активные комнаты, участником которых является пользователь. */
    public List<Room> getUserRooms(String username) {
        User user = userService.getUser(username);
        return roomRepository.findAllByMembersContainingAndIsActiveTrue(user);
    }

    /**
     * Allows a user to leave a room.
     * If the room becomes empty after the user leaves, it is marked as inactive.
     *
     * Позволяет пользователю покинуть комнату.
     * Если комната становится пустой после ухода пользователя, она помечается как неактивная.
     *
     * @param username  the username of the user leaving the room
     *                  имя пользователя, покидающего комнату
     * @param roomId    the unique identifier of the room
     *                  уникальный идентификатор комнаты
     * @throws UserNotFoundException if user with the given username does not exist
     *                               если пользователь с указанным именем не существует
     * @throws RoomNotFoundException if room with the given ID does not exist
     *                               если комната с указанным идентификатором не существует
     */
    public void leaveRoom(String username, long roomId) {
        User user = userService.getUser(username);
        Room room = getRoom(roomId);

        room.getMembers().remove(user);

        if (room.getMembers().isEmpty()) {
            room.setIsActive(false);
        }

        roomRepository.save(room);
    }

    /**
     * Обновляет данные комнаты.
     * Updates room data.
     */
    @Transactional
    public Room updateRoom(Long roomId, String username, String newName) {
        User user = userService.getUser(username);
        Room room = getRoom(roomId);

        // Проверяем, что пользователь является участником комнаты
        boolean isMember = room.getMembers().stream()
                .anyMatch(member -> member.getId().equals(user.getId()));
        if (!isMember) {
            throw new InsufficientPermissionsException("Пользователь не является участником комнаты");
        }

        if (newName != null && !newName.isEmpty()) {
            room.setName(newName);
        }

        return roomRepository.save(room);
    }

    /**
     * Удаляет комнату (помечает как неактивную и удаляет всех участников).
     * Deletes a room (marks as inactive and removes all members).
     */
    @Transactional
    public void deleteRoom(Long roomId, String username) {
        User user = userService.getUser(username);
        Room room = getRoom(roomId);

        // Проверяем, что пользователь является участником комнаты
        boolean isMember = room.getMembers().stream()
                .anyMatch(member -> member.getId().equals(user.getId()));
        if (!isMember) {
            throw new InsufficientPermissionsException("Пользователь не является участником комнаты");
        }

        // Помечаем комнату как неактивную и очищаем участников
        room.setIsActive(false);
        room.getMembers().clear();
        roomRepository.save(room);
    }
}
