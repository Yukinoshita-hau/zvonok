package com.zvonok.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class CommonController {
    @GetMapping("/health")
    public String healthCheck() {
        return "{\"status\":\"UP\"}";
    }
}
