package com.zvonok.controller;

import com.zvonok.model.User;
import com.zvonok.service.UserService;
import com.zvonok.service.dto.CreateUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public User getUserById(@PathVariable long id) {
        return userService.getUser(id);
    }

    @PostMapping("/")
    public User createUser(@RequestBody CreateUserDto userDto) {
        return userService.createUser(userDto);
    }
}
