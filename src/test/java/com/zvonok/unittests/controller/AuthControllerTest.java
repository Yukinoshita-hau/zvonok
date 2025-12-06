package com.zvonok.unittests.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.zvonok.controller.AuthController;
import com.zvonok.exception.InvalidRefreshTokenException;
import com.zvonok.exception.InvalidUserOrPasswordException;
import com.zvonok.exception.RefreshTokenExpiredException;
import com.zvonok.exception.RefreshTokenRevokedException;
import com.zvonok.exception.UserWIthThisUsernameAlreadyExistException;
import com.zvonok.exception.UserWithThisEmailAlreadyExistException;
import com.zvonok.exception_handler.GlobalExceptionHandler;
import com.zvonok.exception_handler.enumeration.HttpResponseMessage;
import com.zvonok.security.JwtTokenProvider;
import com.zvonok.service.AuthService;
import com.zvonok.service.dto.AuthResponse;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // вырубание Spring Security фильтров
@Import(GlobalExceptionHandler.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        authResponse = new AuthResponse(
            "test-access-token", 
            "test-refresh-token", 
            "Bearer", 
            3600000L);

    }

    // Register
    static String registerRequestJson(String username, String email, String password) {
        return 
        """
            {
                "username": "%s",
                "email": "%s",
                "password": "%s"
            } 
        """.formatted(username, email, password);
    }

    @Test
    void register_shouldReturn200_whenValidRequest() throws Exception {
        // Arrange
        when(authService.register(anyString(), anyString(), anyString())).thenReturn(authResponse);
        
        // Act
        mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerRequestJson("testUser", "testEmail@example.ru", "qwerty123")))
        // Assert
            .andExpect(status().isOk())
            .andExpectAll(
                jsonPath("$.accessToken").value("test-access-token"),
                jsonPath("$.refreshToken").value("test-refresh-token"),
                jsonPath("tokenType").value("Bearer")
            );
    }

    @ParameterizedTest
    @MethodSource("registerExceptionCases")
    void register_shouldThrowException(RuntimeException exception, String expectedMessage, String content) throws Exception {
        // Arrange
        when(authService.register(anyString(), anyString(), anyString())).thenThrow(exception);

        // Act
        mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(content))
        // Assert
            .andExpect(status().isConflict())
            .andExpectAll(
                jsonPath("$.message").value(expectedMessage),
                jsonPath("$.status").value(HttpStatus.CONFLICT.value())
            );
    }

    static Stream<Arguments> registerExceptionCases() {
        return Stream.of(
            Arguments.of(
                new UserWIthThisUsernameAlreadyExistException(
                    HttpResponseMessage.HTTP_USER_WITH_THIS_USERNAME_ALREADY_EXIST_RESPONSE_MESSAGE.getMessage()), 
                HttpResponseMessage.HTTP_USER_WITH_THIS_USERNAME_ALREADY_EXIST_RESPONSE_MESSAGE.getMessage(), 
                registerRequestJson("existUsername", "testEmail@example.ru", "qwerty123")),
            Arguments.of(
                new UserWithThisEmailAlreadyExistException(
                    HttpResponseMessage.HTTP_USER_WITH_THIS_EMAIL_ALREADY_EXIST_RESPONSE_MESSAGE.getMessage()), 
                HttpResponseMessage.HTTP_USER_WITH_THIS_EMAIL_ALREADY_EXIST_RESPONSE_MESSAGE.getMessage(), 
                registerRequestJson("testUser", "existEmail@example.ru", "qwerty123"))
        );
    }

    // Login
    String loginRequestJson(String usernameOrEmail, String password) {
        return
            """
                {
                    "usernameOrEmail": "%s",
                    "password": "%s"     
                }     
            """.formatted(usernameOrEmail, password);
    }

    @Test
    void login_shouldReturn200_whenValidRequest() throws Exception {
        // Arrange
        when(authService.login(anyString(), anyString())).thenReturn(authResponse);

        // Act
        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginRequestJson("usernameOrEmail", "password123")))
        // Assert
            .andExpect(status().isOk())
            .andExpectAll(
                jsonPath("$.accessToken").value("test-access-token"),
                jsonPath("$.refreshToken").value("test-refresh-token"),
                jsonPath("tokenType").value("Bearer")
            );
    }

    @Test
    void login_shouldThrowException_whenUserOrPasswordInvalid() throws Exception {
        // Arrange
        when(authService.login(anyString(), anyString()))
            .thenThrow(new InvalidUserOrPasswordException(
                HttpResponseMessage.HTTP_INVALID_USER_OR_PASSWORD_RESPONSE_MESSAGE.getMessage()));
        
        // Act
        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginRequestJson("usernameOrEmail", "password123")))
        // Assert
            .andExpect(status().isUnauthorized())
            .andExpectAll(
                jsonPath("$.message").value(HttpResponseMessage.HTTP_INVALID_USER_OR_PASSWORD_RESPONSE_MESSAGE.getMessage()),
                jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value())
            );
    }
    
    // Refresh
    static String refreshRequestJson(String refreshToken) {
        return 
            """
                {
                    "refreshToken": "$s" 
                }   
            """.formatted(refreshToken);
    }


    @Test
    void refresh_shouldReturn200_whenValidRequest() throws Exception {
        // Arrange
        when(authService.refresh(anyString())).thenReturn(authResponse);

        // Act
        mockMvc.perform(post("/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(refreshRequestJson("refresh-token")))
        // Assert
            .andExpect(status().isOk())
            .andExpectAll(
                jsonPath("$.accessToken").value("test-access-token"),
                jsonPath("$.refreshToken").value("test-refresh-token"),
                jsonPath("tokenType").value("Bearer")
            );
    }

    @ParameterizedTest
    @MethodSource("refreshExceptionCases")
    void refresh_shouldThrowException(RuntimeException exception, String exceptionMessage, String content) throws Exception {
        // Arrange
        when(authService.refresh(anyString())).thenThrow(exception);

        // Act
        mockMvc.perform(post("/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(content))
        // Assert
            .andExpect(status().isUnauthorized())
            .andExpectAll(
                jsonPath("$.message").value(exceptionMessage),
                jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value())
            );
    }

    static private Stream<Arguments> refreshExceptionCases() {
        return Stream.of(
            Arguments.of(
                new InvalidRefreshTokenException(
                    HttpResponseMessage.HTTP_INVALID_REFRESH_TOKEN_RESPONSE_MESSAGE.getMessage()),
                HttpResponseMessage.HTTP_INVALID_REFRESH_TOKEN_RESPONSE_MESSAGE.getMessage(),
                refreshRequestJson("invalid-refresh-token")
            ),
            Arguments.of(
                new RefreshTokenRevokedException(
                    HttpResponseMessage.HTTP_REFRESH_TOKEN_REVOKED_RESPONSE_MESSAGE.getMessage()),
                HttpResponseMessage.HTTP_REFRESH_TOKEN_REVOKED_RESPONSE_MESSAGE.getMessage(),
                refreshRequestJson("revoked-refresh-token")
            ),
            Arguments.of(
                new RefreshTokenExpiredException(
                    HttpResponseMessage.HTTP_REFRESH_TOKEN_EXPIRED_RESPONSE_MESSAGE.getMessage()),
                HttpResponseMessage.HTTP_REFRESH_TOKEN_EXPIRED_RESPONSE_MESSAGE.getMessage(),
                refreshRequestJson("expired-refresh-token")
            )
        );
    }
}