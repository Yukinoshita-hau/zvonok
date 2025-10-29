package com.zvonok.service;

import com.zvonok.exception.UserNotFoundException;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.model.User;
import com.zvonok.repository.UserRepository;
import com.zvonok.service.dto.CreateUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUser(Long id)  {

        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(HttpResponseMessage.HTTP_USER_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));
    }

    public User getUser(String username) {

        return userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(HttpResponseMessage.HTTP_USER_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));
    }

    public User createUser(CreateUserDto userDto) {

        userRepository.findByUsername(userDto.getUsername()).orElseThrow(() -> new UserNotFoundException(HttpResponseMessage.HTTP_USER_NOT_FOUND_RESPONSE_MESSAGE.getMessage()));

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setDisplayName(userDto.getDisplayName());
        user.setLastSeenAt(LocalDateTime.now());
        user.setAvatarUrl("");

        return userRepository.save(user);
    }
}
