package com.example.lab10.controller;

import com.example.lab10.repo.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public Map<String, Object> me() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        return userRepository.findByUsername(username)
                .map(user -> Map.<String, Object>of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                ))
                .orElse(Map.of(
                        "username", username,
                        "roles", auth.getAuthorities()
                ));
    }

    @GetMapping("/profile")
    public Map<String, Object> profile() {
        return me();
    }
}
