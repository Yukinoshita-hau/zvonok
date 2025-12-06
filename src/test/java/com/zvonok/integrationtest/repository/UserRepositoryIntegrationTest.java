package com.zvonok.integrationtest.repository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.zvonok.model.User;
import com.zvonok.repository.UserRepository;

@DataJpaTest
public class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository repository;

    @Test
    void findUserByUsernameOrEmail_shouldFindUser_whenExists(){

        // Arrange
        final User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setIsEmailVerified(false);

        // Act
        final User savedUser = repository.save(user);
        final Optional<User> actualUsername = repository.findByUsernameOrEmail(savedUser.getUsername());
        final Optional<User> actualEmail = repository.findByUsernameOrEmail(savedUser.getEmail());

        // Assert
        assertTrue(actualUsername.isPresent());
        assertAll(
            () -> assertEquals(savedUser.getId(), actualUsername.get().getId()),
            () -> assertEquals(savedUser.getUsername(), actualUsername.get().getUsername()),
            () -> assertEquals(savedUser.getEmail(), actualUsername.get().getEmail())
        );
        
        assertTrue(actualEmail.isPresent());
        assertAll(
            () -> assertEquals(savedUser.getId(), actualEmail.get().getId()),
            () -> assertEquals(savedUser.getUsername(), actualEmail.get().getUsername()),
            () -> assertEquals(savedUser.getEmail(), actualEmail.get().getEmail())
        );
    }

    @Test
    void findUserByUsernameOrEmail_shouldReturnEmpty_whenNotExist() {
        // Act
        Optional<User> result = repository.findByUsernameOrEmail("noneExistUser");

        // Assert
        assertTrue(result.isEmpty());
    }
}