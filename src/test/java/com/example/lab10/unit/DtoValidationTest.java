package com.example.lab10.unit;

import com.example.lab10.dto_.UserCreateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DTO validation.
 */
@DisplayName("DTO Validation Unit Tests")
class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid UserCreateRequest should have no violations")
    void validUserCreateRequest_ShouldHaveNoViolations() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("validuser");
        request.setEmail("test@example.com");
        request.setPassword("SecureP@ss1");

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    @DisplayName("UserCreateRequest with blank username should have violation")
    void blankUsername_ShouldHaveViolation() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("");
        request.setEmail("test@example.com");
        request.setPassword("SecureP@ss1");

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "Blank username should have violation");
    }

    @Test
    @DisplayName("UserCreateRequest with invalid email should have violation")
    void invalidEmail_ShouldHaveViolation() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("validuser");
        request.setEmail("invalid-email");
        request.setPassword("SecureP@ss1");

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "Invalid email should have violation");
    }

    @Test
    @DisplayName("UserCreateRequest with weak password should have violation")
    void weakPassword_ShouldHaveViolation() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("validuser");
        request.setEmail("test@example.com");
        request.setPassword("weak");

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "Weak password should have violation");
    }
}
