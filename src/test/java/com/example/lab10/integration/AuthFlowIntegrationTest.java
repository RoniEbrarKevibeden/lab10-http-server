package com.example.lab10.integration;

import com.example.lab10.dto_.LoginRequest;
import com.example.lab10.dto_.UserCreateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for authentication DTOs and validation.
 * Tests: request validation for login and register.
 */
@DisplayName("Auth DTO Validation Tests")
class AuthFlowIntegrationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("UserCreateRequest with weak password should have violations")
    void register_WithWeakPassword_ShouldHaveViolations() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("weak");

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "Weak password should cause violations");
    }

    @Test
    @DisplayName("UserCreateRequest with valid data should have no violations")
    void register_WithValidData_ShouldHaveNoViolations() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("validuser");
        request.setEmail("valid@example.com");
        request.setPassword("SecureP@ss1");

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty(), "Valid data should have no violations");
    }

    @Test
    @DisplayName("LoginRequest with blank username should have violations")
    void login_WithBlankUsername_ShouldHaveViolations() {
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("SomeP@ss1");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "Blank username should cause violations");
    }

    @Test
    @DisplayName("LoginRequest with blank password should have violations")
    void login_WithBlankPassword_ShouldHaveViolations() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "Blank password should cause violations");
    }

    @Test
    @DisplayName("LoginRequest with valid data should have no violations")
    void login_WithValidData_ShouldHaveNoViolations() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("ValidP@ss1");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty(), "Valid login data should have no violations");
    }
}
