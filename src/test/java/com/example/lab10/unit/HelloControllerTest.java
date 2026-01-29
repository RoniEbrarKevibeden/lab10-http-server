package com.example.lab10.unit;

import com.example.lab10.controller.HelloController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HelloController.
 */
@DisplayName("HelloController Unit Tests")
class HelloControllerTest {

    private final HelloController helloController = new HelloController();

    @Test
    @DisplayName("GET /hello should return greeting message")
    void hello_ShouldReturnGreetingMessage() {
        String result = helloController.hello();
        
        assertNotNull(result);
        assertTrue(result.contains("Hello"));
    }
}
