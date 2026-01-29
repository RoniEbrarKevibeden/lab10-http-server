package com.example.lab10.controller;

import com.example.lab10.dto_.UserCreateRequest;
import com.example.lab10.entity.AppUser;
import com.example.lab10.repo.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final UserRepository userRepository;

    public ApiController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // GET all users (ADMIN only)
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<Map<String, Object>> getUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    userMap.put("email", user.getEmail());
                    userMap.put("role", user.getRole());
                    return userMap;
                })
                .collect(Collectors.toList());
    }

    // GET + query param
    @GetMapping("/echo")
    public Map<String, Object> echo(@RequestParam(defaultValue = "empty") String msg) {
        Map<String, Object> res = new HashMap<>();
        res.put("msg", msg);
        return res;
    }

    // POST + echo body
    @PostMapping(value = "/echo", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String echoPost(@RequestBody String body) {
        return body;
    }

    // GET + header read
    @GetMapping("/headers")
    public Map<String, Object> headers(
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Map<String, Object> res = new HashMap<>();
        res.put("userAgent", userAgent);
        res.put("authorizationPresent", authorization != null);
        return res;
    }

    // GET current user profile
    @GetMapping("/profile")
    public Map<String, Object> profile() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> res = new HashMap<>();
        res.put("username", auth.getName());
        res.put("roles", auth.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.toList()));
        return res;
    }

    // POST + JSON body + validation
    @PostMapping(value = "/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> createUser(@Valid @RequestBody UserCreateRequest body) {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "created");
        res.put("username", body.getUsername());
        res.put("email", body.getEmail());
        return res;
    }
}
