package com.example.lab10.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UsernameValidator implements ConstraintValidator<Username, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true; // @NotBlank already checking
        return value.matches("^[a-zA-Z0-9_]{3,20}$");
    }
}
