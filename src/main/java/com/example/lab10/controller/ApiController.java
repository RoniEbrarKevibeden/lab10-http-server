package com.example.lab10.controller;

import com.example.lab10.dto_.UserCreateRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    // GET + query param
    @GetMapping("/echo")
    public Map<String, Object> echo(@RequestParam(defaultValue = "empty") String msg) {
        Map<String, Object> res = new HashMap<>();
        res.put("msg", msg);
        return res;
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
