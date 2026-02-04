package com.example.lab10.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    
    // Password must have:
    // - At least 8 characters
    // - At least one uppercase letter
    // - At least one lowercase letter
    // - At least one digit
    // - At least one special character (@#$%^&+=!)
    private static final String PASSWORD_PATTERN = 
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // @NotBlank handles null check
        }
        return value.matches(PASSWORD_PATTERN);
    }
}
