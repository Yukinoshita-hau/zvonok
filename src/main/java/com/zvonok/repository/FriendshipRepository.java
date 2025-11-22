package com.zvonok.repository;

import com.zvonok.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    boolean existsByUserOneIdAndUserTwoId(Long userOneId, Long userTwoId);

    Optional<Friendship> findByUserOneIdAndUserTwoId(Long userOneId, Long userTwoId);

    List<Friendship> findByUserOneIdOrUserTwoId(Long userOneId, Long userTwoId);

    void deleteByUserOneIdAndUserTwoId(Long userOneId, Long userTwoId);
}

