package com.example.lab10.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/me")
    public Map<String, Object> me() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        return Map.of(
                "username", auth.getName(),
                "roles", auth.getAuthorities()
        );
    }
}
