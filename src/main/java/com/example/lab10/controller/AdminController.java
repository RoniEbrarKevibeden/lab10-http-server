package com.example.lab10.controller;

import com.example.lab10.repo.NoteRepository;
import com.example.lab10.repo.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;

    public AdminController(UserRepository userRepository, NoteRepository noteRepository) {
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
    }

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of("ok", true, "msg", "ADMIN OK");
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        long totalUsers = userRepository.count();
        long totalNotes = noteRepository.count();

        return Map.of(
                "totalUsers", totalUsers,
                "totalNotes", totalNotes
        );
    }

    @GetMapping("/users")
    public Map<String, Object> listUsers() {
        var users = userRepository.findAll().stream()
                .map(user -> Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                ))
                .collect(Collectors.toList());

        return Map.of("users", users, "count", users.size());
    }
}
