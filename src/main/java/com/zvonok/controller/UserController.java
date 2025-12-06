package com.zvonok.controller;

import com.zvonok.model.User;
import com.zvonok.service.UserService;
import com.zvonok.service.dto.CreateUserDto;
import com.zvonok.service.dto.UpdateUserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @GetMapping("/search")
    public ResponseEntity<User> searchUserByUsername(@RequestParam String username) {
        return ResponseEntity.ok(userService.getUser(username));
    }

    @PostMapping("/")
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserDto userDto) {
        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
