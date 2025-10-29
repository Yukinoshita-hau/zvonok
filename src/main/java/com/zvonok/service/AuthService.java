package com.zvonok.service;

import com.zvonok.exception.InvalidUserOrPasswordException;
import com.zvonok.exception.UserWIthThisUsernameAlreadyExistException;
import com.zvonok.exception.UserWithThisEmailAlreadyExistException;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.model.User;
import com.zvonok.repository.UserRepository;
import com.zvonok.security.JwtTokenProvider;
import com.zvonok.service.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse register(String username, String email, String password, String displayName) {
        log.info("Регистрация пользователя: {}", username);

        if (userRepository.existsByUsername(username)) {
            throw new UserWIthThisUsernameAlreadyExistException(HttpResponseMessage.HTTP_USER_WITH_THIS_USERNAME_ALREADY_EXIST_RESPONSE_MESSAGE.getMessage());
        }

        if (userRepository.existsByEmail(email)) {
            throw new UserWithThisEmailAlreadyExistException(HttpResponseMessage.HTTP_USER_WITH_THIS_EMAIL_ALREADY_EXIST_RESPONSE_MESSAGE.getMessage());
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setDisplayName(displayName != null ? displayName: username);
        user.setIsEmailVerified(false);
        user.setLastSeenAt(LocalDateTime.now());
        user.setAvatarUrl("");

        User savedUser = userRepository.save(user);
        log.info("Пользователь зарегистрирован: {}", savedUser.getUsername());

        String token = jwtTokenProvider.generateToken(savedUser.getUsername(), savedUser.getId());

        return new AuthResponse(token, "Bearer");
    }

    public AuthResponse login(String username, String password) {
        log.info("Попытка входа: {}", username);

        // что бы кент не понел что именно username не верен мы кидаем неверно имя либо пароль
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidUserOrPasswordException(HttpResponseMessage.HTTP_INVALID_USER_OR_PASSWORD_RESPONSE_MESSAGE.getMessage()));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidUserOrPasswordException(HttpResponseMessage.HTTP_INVALID_USER_OR_PASSWORD_RESPONSE_MESSAGE.getMessage());
        }

        user.setLastSeenAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Успешый вход: {}", user.getUsername());

        String token = jwtTokenProvider.generateToken(user.getUsername(),user.getId());

        return new AuthResponse(token, "Bearer");

    }
}
