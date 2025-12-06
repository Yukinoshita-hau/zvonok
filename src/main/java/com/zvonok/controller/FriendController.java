package com.zvonok.controller;

import com.zvonok.controller.dto.FriendRequestResponse;
import com.zvonok.controller.dto.FriendResponse;
import com.zvonok.controller.dto.SendFriendRequestRequest;
import com.zvonok.model.FriendRequest;
import com.zvonok.model.Friendship;
import com.zvonok.model.User;
import com.zvonok.security.dto.UserPrincipal;
import com.zvonok.service.FriendService;
import com.zvonok.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<FriendResponse>> getFriends(@AuthenticationPrincipal UserPrincipal principal) {
        User currentUser = getCurrentUser(principal);
        List<FriendResponse> friends = friendService.getFriendships(currentUser.getId()).stream()
                .map(friendship -> toFriendResponse(friendship, currentUser))
                .toList();
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/requests/incoming")
    public ResponseEntity<List<FriendRequestResponse>> getIncomingRequests(@AuthenticationPrincipal UserPrincipal principal) {
        User currentUser = getCurrentUser(principal);
        List<FriendRequestResponse> requests = friendService.getIncomingRequests(currentUser.getId()).stream()
                .map(this::toFriendRequestResponse)
                .toList();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/requests/outgoing")
    public ResponseEntity<List<FriendRequestResponse>> getOutgoingRequests(@AuthenticationPrincipal UserPrincipal principal) {
        User currentUser = getCurrentUser(principal);
        List<FriendRequestResponse> requests = friendService.getOutgoingRequests(currentUser.getId()).stream()
                .map(this::toFriendRequestResponse)
                .toList();
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/requests")
    public ResponseEntity<FriendRequestResponse> sendFriendRequest(
            @Valid @RequestBody SendFriendRequestRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        User currentUser = getCurrentUser(principal);
        User receiver = userService.getUser(request.getReceiverUsername());
        FriendRequest friendRequest = friendService.sendFriendRequest(currentUser.getId(), receiver.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toFriendRequestResponse(friendRequest));
    }

    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<FriendResponse> acceptFriendRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserPrincipal principal) {
        User currentUser = getCurrentUser(principal);
        Friendship friendship = friendService.acceptFriendRequest(requestId, currentUser.getId());
        return ResponseEntity.ok(toFriendResponse(friendship, currentUser));
    }

    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<FriendRequestResponse> rejectFriendRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserPrincipal principal) {
        User currentUser = getCurrentUser(principal);
        FriendRequest friendRequest = friendService.rejectFriendRequest(requestId, currentUser.getId());
        return ResponseEntity.ok(toFriendRequestResponse(friendRequest));
    }

    @PostMapping("/requests/{requestId}/cancel")
    public ResponseEntity<FriendRequestResponse> cancelFriendRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserPrincipal principal) {
        User currentUser = getCurrentUser(principal);
        FriendRequest friendRequest = friendService.cancelFriendRequest(requestId, currentUser.getId());
        return ResponseEntity.ok(toFriendRequestResponse(friendRequest));
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> removeFriend(
            @PathVariable Long friendId,
            @AuthenticationPrincipal UserPrincipal principal) {
        User currentUser = getCurrentUser(principal);
        friendService.removeFriend(currentUser.getId(), friendId);
        return ResponseEntity.noContent().build();
    }

    private FriendResponse toFriendResponse(Friendship friendship, User currentUser) {
        User friend = Objects.equals(friendship.getUserOne().getId(), currentUser.getId())
                ? friendship.getUserTwo()
                : friendship.getUserOne();

        return FriendResponse.builder()
                .friendshipId(friendship.getId())
                .friendId(friend.getId())
                .friendUsername(friend.getUsername())
                .friendEmail(friend.getEmail())
                .friendAvatarUrl(friend.getAvatarUrl())
                .friendStatus(friend.getStatus())
                .friendshipSince(friendship.getCreatedAt())
                .build();
    }

    private FriendRequestResponse toFriendRequestResponse(FriendRequest friendRequest) {
        return FriendRequestResponse.builder()
                .requestId(friendRequest.getId())
                .senderId(friendRequest.getSender().getId())
                .senderUsername(friendRequest.getSender().getUsername())
                .receiverId(friendRequest.getReceiver().getId())
                .receiverUsername(friendRequest.getReceiver().getUsername())
                .status(friendRequest.getStatus())
                .createdAt(friendRequest.getCreatedAt())
                .updatedAt(friendRequest.getUpdatedAt())
                .build();
    }

    private User getCurrentUser(UserPrincipal principal) {
        return userService.getUser(principal.getUsername());
    }
}

