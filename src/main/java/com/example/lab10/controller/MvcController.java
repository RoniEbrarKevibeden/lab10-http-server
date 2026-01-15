package com.example.lab10.controller;

import com.example.lab10.dto_.ContactForm;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/mvc")
public class MvcController {

    // FORM (x-www-form-urlencoded)
    @PostMapping(value = "/contact", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Map<String, Object> submitForm(@Valid @ModelAttribute ContactForm form) {
        return Map.of(
                "ok", true,
                "name", form.getName(),
                "email", form.getEmail()
        );
    }
}
