package com.zvonok.controller;

import com.zvonok.controller.dto.MessageResponse;
import com.zvonok.controller.dto.UpdateMessageRequest;
import com.zvonok.model.Message;
import com.zvonok.security.dto.UserPrincipal;
import com.zvonok.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/{messageId}")
    public ResponseEntity<Message> getMessage(@PathVariable Long messageId) {
        return ResponseEntity.ok(messageService.getMessage(messageId));
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<MessageResponse> updateMessage(
            @PathVariable Long messageId,
            @Valid @RequestBody UpdateMessageRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        MessageResponse response = messageService.editMessage(messageId, principal.getUsername(), request.getContent());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
           @PathVariable Long messageId,
            @AuthenticationPrincipal UserPrincipal principal) {
        messageService.deleteMessage(messageId, principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}

