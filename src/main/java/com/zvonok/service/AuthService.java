package com.zvonok.service;

import com.zvonok.exception.InvalidUserOrPasswordException;
import com.zvonok.exception.UserWIthThisUsernameAlreadyExistException;
import com.zvonok.exception.UserWithThisEmailAlreadyExistException;
import com.zvonok.model.RefreshToken;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.model.User;
import com.zvonok.repository.UserRepository;
import com.zvonok.security.JwtTokenProvider;
import com.zvonok.service.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for handling user authentication operations (registration and login).
 * Сервис для обработки операций аутентификации пользователей (регистрация и вход).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    /**
     * Registers a new user in the system.
     * Validates username and email uniqueness, encrypts password, and generates JWT token.
     *
     * Регистрирует нового пользователя в системе.
     * Проверяет уникальность имени пользователя и email, шифрует пароль и генерирует JWT токен.
     *
     * @param username     the username for the new user
     *                     имя пользователя для нового пользователя
     * @param email        the email address for the new user
     *                     адрес электронной почты для нового пользователя
     * @param password     the plain text password (will be encrypted)
     *                     пароль в открытом виде (будет зашифрован)
     * @return AuthResponse containing the JWT token and token type
     *         ответ, содержащий JWT токен и тип токена
     * @throws UserWIthThisUsernameAlreadyExistException if username already exists
     *                                                   если имя пользователя уже существует
     * @throws UserWithThisEmailAlreadyExistException    if email already exists
     *                                                   если email уже существует
     */
    public AuthResponse register(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new UserWIthThisUsernameAlreadyExistException(
                    HttpResponseMessage.HTTP_USER_WITH_THIS_USERNAME_ALREADY_EXIST_RESPONSE_MESSAGE.getMessage());
        }

        if (userRepository.existsByEmail(email)) {
            throw new UserWithThisEmailAlreadyExistException(
                    HttpResponseMessage.HTTP_USER_WITH_THIS_EMAIL_ALREADY_EXIST_RESPONSE_MESSAGE.getMessage());
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setIsEmailVerified(false);
        user.setLastSeenAt(LocalDateTime.now());
        user.setAvatarUrl("");

        User savedUser = userRepository.save(user);
        return buildAuthResponse(savedUser);
    }

    /**
     * Authenticates a user and generates a JWT token.
     * Validates username and password, updates last seen timestamp.
     * For security reasons, if either username or password is incorrect, a generic error is thrown.
     *
     * Аутентифицирует пользователя и генерирует JWT токен.
     * Проверяет имя пользователя и пароль, обновляет время последнего посещения.
     * В целях безопасности, если имя пользователя или пароль неверны, выбрасывается общая ошибка.
     *
     * @param username  the username to authenticate
     *                  имя пользователя для аутентификации
     * @param password  the plain text password to verify
     *                  пароль в открытом виде для проверки
     * @return AuthResponse containing the JWT token and token type
     *         ответ, содержащий JWT токен и тип токена
     * @throws InvalidUserOrPasswordException if username doesn't exist or password is incorrect
     *                                        если имя пользователя не существует или пароль неверен
     */
    public AuthResponse login(String username, String password) {
        // Для безопасности не указываем, что именно неверно (username или password)
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidUserOrPasswordException(
                        HttpResponseMessage.HTTP_INVALID_USER_OR_PASSWORD_RESPONSE_MESSAGE.getMessage()));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidUserOrPasswordException(
                    HttpResponseMessage.HTTP_INVALID_USER_OR_PASSWORD_RESPONSE_MESSAGE.getMessage());
        }

        user.setLastSeenAt(LocalDateTime.now());
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken validToken = refreshTokenService.validate(refreshTokenValue);
        User user = validToken.getUser();
        RefreshToken rotated = refreshTokenService.rotate(validToken);

        String accessToken = jwtTokenProvider.generateToken(user.getUsername(), user.getId());

        return new AuthResponse(
                accessToken,
                rotated.getToken(),
                "Bearer",
                jwtTokenProvider.getJwtExpirationMs());
    }

    public void logout(String refreshTokenValue) {
        refreshTokenService.revoke(refreshTokenValue);
    }

    public void logoutFromAllDevices(Long userId) {
        refreshTokenService.revokeAllForUser(userId);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateToken(user.getUsername(), user.getId());
        RefreshToken refreshToken = refreshTokenService.createToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                jwtTokenProvider.getJwtExpirationMs());
    }
}
