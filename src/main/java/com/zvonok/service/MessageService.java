package com.zvonok.service;

import com.zvonok.controller.dto.ChannelMessageResponse;
import com.zvonok.exception.InsufficientPermissionsException;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.model.Channel;
import com.zvonok.service.dto.EventType;
import com.zvonok.controller.dto.MessageResponse;
import com.zvonok.model.Message;
import com.zvonok.model.Room;
import com.zvonok.model.User;
import com.zvonok.model.enumeration.MessageType;
import com.zvonok.repository.MessageRepository;
import com.zvonok.repository.RoomRepository;
import com.zvonok.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;
    private final UserService userService;
    private final ChannelService channelService;
    private final PermissionService permissionService;

    public MessageResponse sendPrivateMessage(String senderUsername, String receiverUsername, String content) {

        Room privateRoom = roomService.createOrGetPrivateRoom(senderUsername, receiverUsername);

        User sender = userRepository.findByUsername(senderUsername).get();

        Message message = new Message();
        message.setSender(sender);
        message.setRoom(privateRoom);
        message.setContent(content);
        message.setType(MessageType.DEFAULT);
        message.setReplyToMessageId(null);
        message.setEditedAt(null);
        message.setDeletedAt(null);
        message.setSentAt(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);

        MessageResponse response = new MessageResponse();
        response.setId(savedMessage.getId());
        response.setContent(content);
        response.setSenderUsername(sender.getUsername());
        response.setSenderDisplayName(sender.getDisplayName());
        response.setSentAt(savedMessage.getSentAt());
        response.setMessageType(savedMessage.getType());
        response.setRoomId(privateRoom.getId());
        response.setEventType(EventType.MESSAGE);

        privateRoom.getMembers().forEach(member -> {
            messagingTemplate.convertAndSendToUser(
                    member.getUsername(),
                    "/queue/messages",
                    response
            );
            log.debug("Сообщение отправлено пользователю: {}", member.getUsername());
        });

        return response;
    }

    public MessageResponse sendGroupMessage(String senderUsername, long roomId, String content) {

        Room groupRoom = roomService.getRoom(roomId);

        User sender = userService.getUser(senderUsername);

        Message message = new Message();
        message.setSender(sender);
        message.setRoom(groupRoom);
        message.setContent(content);
        message.setType(MessageType.DEFAULT);
        message.setReplyToMessageId(null);
        message.setEditedAt(null);
        message.setDeletedAt(null);
        message.setSentAt(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);

        MessageResponse response = new MessageResponse();
        response.setId(savedMessage.getId());
        response.setContent(content);
        response.setSenderUsername(sender.getUsername());
        response.setSenderDisplayName(sender.getDisplayName());
        response.setSentAt(savedMessage.getSentAt());
        response.setMessageType(savedMessage.getType());
        response.setRoomId(groupRoom.getId());
        response.setEventType(EventType.MESSAGE);

        messagingTemplate.convertAndSend("/topic/room." + groupRoom.getId(), response);

        return response;
    }

    public ChannelMessageResponse sendChannelMessage(String senderUsername, Long channelId, String content) {

        log.info("📩 Processing channel message: user={}, channelId={}, content='{}'",
                senderUsername, channelId, content);

        try {
            // Получаем пользователя
            User sender = userService.getUser(senderUsername);
            log.debug("✅ Found user: {}", sender.getUsername());

            // Получаем канал
            Channel channel = channelService.getChannel(channelId);
            log.debug("✅ Found channel: {} in folder {}", channel.getName(), channel.getFolder().getName());

            // Проверяем права на отправку сообщений в канал
            if (!permissionService.canUserSendMessages(sender.getId(), channelId)) {
                log.warn("❌ User {} has no permission to send messages to channel {}", senderUsername, channelId);
                throw new InsufficientPermissionsException(HttpResponseMessage.HTTP_INSUFFICIENT_PERMISSIONS_RESPONSE_MESSAGE.getMessage());
            }
            log.debug("✅ User has permission to send messages");

            // Создаем сообщение
            Message message = new Message();
            message.setSender(sender);
            message.setChannel(channel);
            message.setRoom(null);
            message.setContent(content);
            message.setType(MessageType.DEFAULT);
            message.setSentAt(LocalDateTime.now());
            message.setIsEdited(false);
            message.setIsDeleted(false);

            Message savedMessage = messageRepository.save(message);
            log.debug("✅ Message saved with ID: {}", savedMessage.getId());

            // Создаем ответ
            ChannelMessageResponse response = new ChannelMessageResponse();
            response.setId(savedMessage.getId());
            response.setContent(content);
            response.setSenderUsername(sender.getUsername());
            response.setSenderDisplayName(sender.getDisplayName());
            response.setSenderId(sender.getId());
            response.setSentAt(savedMessage.getSentAt());
            response.setMessageType(savedMessage.getType());
            response.setChannelId(channel.getId());
            response.setChannelName(channel.getName());
            response.setServerId(channel.getFolder().getServer().getId());
            response.setEventType(EventType.MESSAGE);
            response.setIsEdited(false);

            // ===== ОТПРАВЛЯЕМ В СТАНДАРТНОМ ФОРМАТЕ =====
            String topicDestination = "/topic/channel." + channelId;
            messagingTemplate.convertAndSend(topicDestination, response);

            log.info("✅ Channel message sent successfully to topic: {} by user: {}",
                    topicDestination, senderUsername);

            return response;

        } catch (Exception e) {
            log.error("❌ Error sending channel message: {}", e.getMessage(), e);
            throw e;
        }
    }

}
