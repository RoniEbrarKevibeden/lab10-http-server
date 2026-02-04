package com.example.lab10.unit;

import com.example.lab10.validation.UsernameValidator;
import com.example.lab10.validation.StrongPasswordValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for custom validators.
 */
@DisplayName("Validation Unit Tests")
class ValidationTest {

    private final UsernameValidator usernameValidator = new UsernameValidator();
    private final StrongPasswordValidator passwordValidator = new StrongPasswordValidator();

    // ==================== Username Validation Tests ====================

    @Test
    @DisplayName("Valid username should pass validation")
    void validUsername_ShouldPass() {
        assertTrue(usernameValidator.isValid("john_doe", null));
        assertTrue(usernameValidator.isValid("user123", null));
        assertTrue(usernameValidator.isValid("Test_User_99", null));
    }

    @Test
    @DisplayName("Username with special characters should fail")
    void usernameWithSpecialChars_ShouldFail() {
        assertFalse(usernameValidator.isValid("john@doe", null));
        assertFalse(usernameValidator.isValid("user name", null));
        assertFalse(usernameValidator.isValid("test!", null));
    }

    @Test
    @DisplayName("Username too short should fail")
    void usernameTooShort_ShouldFail() {
        assertFalse(usernameValidator.isValid("ab", null));
        assertFalse(usernameValidator.isValid("x", null));
    }

    @Test
    @DisplayName("Username too long should fail")
    void usernameTooLong_ShouldFail() {
        assertFalse(usernameValidator.isValid("a".repeat(21), null));
    }

    @Test
    @DisplayName("Null username should pass (let @NotBlank handle it)")
    void nullUsername_ShouldPass() {
        assertTrue(usernameValidator.isValid(null, null));
    }

    // ==================== Password Validation Tests ====================

    @Test
    @DisplayName("Strong password should pass validation")
    void strongPassword_ShouldPass() {
        assertTrue(passwordValidator.isValid("Admin123!", null));
        assertTrue(passwordValidator.isValid("SecureP@ss1", null));
        assertTrue(passwordValidator.isValid("Test#1234", null));
    }

    @Test
    @DisplayName("Password without uppercase should fail")
    void passwordWithoutUppercase_ShouldFail() {
        assertFalse(passwordValidator.isValid("admin123!", null));
    }

    @Test
    @DisplayName("Password without lowercase should fail")
    void passwordWithoutLowercase_ShouldFail() {
        assertFalse(passwordValidator.isValid("ADMIN123!", null));
    }

    @Test
    @DisplayName("Password without digit should fail")
    void passwordWithoutDigit_ShouldFail() {
        assertFalse(passwordValidator.isValid("AdminPass!", null));
    }

    @Test
    @DisplayName("Password without special character should fail")
    void passwordWithoutSpecialChar_ShouldFail() {
        assertFalse(passwordValidator.isValid("Admin12345", null));
    }

    @Test
    @DisplayName("Password too short should fail")
    void passwordTooShort_ShouldFail() {
        assertFalse(passwordValidator.isValid("Ad1!", null));
        assertFalse(passwordValidator.isValid("Pass1@", null));
    }

    @Test
    @DisplayName("Null password should pass (let @NotBlank handle it)")
    void nullPassword_ShouldPass() {
        assertTrue(passwordValidator.isValid(null, null));
    }
}
