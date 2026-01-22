package com.example.lab10.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password", "12345678", "123456789", "qwerty123", "password1",
            "password123", "admin123", "letmein", "welcome1", "monkey123",
            "dragon123", "master123", "login123", "abc12345", "qwertyuiop",
            "passw0rd", "iloveyou", "sunshine1", "princess1", "football1"
    );

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return true;
        }

        if (password.length() < 8) {
            setMessage(context, "Password must be at least 8 characters long");
            return false;
        }

        if (password.length() > 64) {
            setMessage(context, "Password must not exceed 64 characters");
            return false;
        }

        if (!password.matches(".*[A-Z].*")) {
            setMessage(context, "Password must contain at least one uppercase letter");
            return false;
        }

        if (!password.matches(".*[a-z].*")) {
            setMessage(context, "Password must contain at least one lowercase letter");
            return false;
        }

        if (!password.matches(".*[0-9].*")) {
            setMessage(context, "Password must contain at least one digit");
            return false;
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            setMessage(context, "Password must contain at least one special character");
            return false;
        }

        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            setMessage(context, "Password is too common. Please choose a stronger password");
            return false;
        }

        return true;
    }

    private void setMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
