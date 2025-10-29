package com.zvonok.controller;

import com.zvonok.controller.dto.ChannelMessageResponse;
import com.zvonok.controller.dto.MessageResponse;
import com.zvonok.security.dto.UserPrincipal;
import com.zvonok.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final MessageService messageService;

    @MessageMapping("/private/{receiverUsername}")
    public MessageResponse sendPrivateMessage(@DestinationVariable String receiverUsername,
                                                         UserPrincipal principal,
                                                         @Payload String content) {

        return messageService.sendPrivateMessage(principal.getName(), receiverUsername, content);
    }

    @MessageMapping("/group/{roomId}")
    public MessageResponse sendGroupMessage(@DestinationVariable Long roomId,
                                            UserPrincipal principal,
                                            @Payload String content) {

        return messageService.sendGroupMessage(principal.getName(), roomId, content);
    }

    @MessageMapping("/channel/{channelId}")
    public ChannelMessageResponse sendChannelMessage(@DestinationVariable Long channelId,
                                                     UserPrincipal principal,
                                                     @Payload String content) {

        log.info("Sending channel message to channel {} by user {}", channelId, principal.getUsername());

        return messageService.sendChannelMessage(principal.getUsername(), channelId, content);
    }
}
