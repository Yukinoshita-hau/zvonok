package com.zvonok.repository;

import com.zvonok.model.FriendRequest;
import com.zvonok.model.enumeration.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    boolean existsBySenderIdAndReceiverIdAndStatusIn(Long senderId, Long receiverId, Collection<FriendRequestStatus> statuses);

    Optional<FriendRequest> findBySenderIdAndReceiverIdAndStatus(Long senderId, Long receiverId, FriendRequestStatus status);

    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, FriendRequestStatus status);

    List<FriendRequest> findBySenderIdAndStatus(Long senderId, FriendRequestStatus status);

    List<FriendRequest> findByReceiverId(Long receiverId);

    List<FriendRequest> findBySenderId(Long senderId);
}

