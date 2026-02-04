package com.example.lab10;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic application tests.
 */
@DisplayName("Application Tests")
class Lab10ApplicationTests {

	@Test
	@DisplayName("Application main class should exist")
	void applicationClassExists() {
		assertNotNull(Lab10Application.class);
	}

	@Test
	@DisplayName("Application should have main method")
	void mainMethodExists() throws NoSuchMethodException {
		assertNotNull(Lab10Application.class.getMethod("main", String[].class));
	}
}
