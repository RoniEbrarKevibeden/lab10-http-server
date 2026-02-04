package com.example.lab10.integration;

import com.example.lab10.security.SecurityHeadersFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

/**
 * Unit tests for Security Headers Filter.
 * Verifies filter adds: X-Content-Type-Options, X-Frame-Options, CSP, Referrer-Policy
 */
@DisplayName("Security Headers Filter Tests")
class SecurityHeadersIntegrationTest {

    private final SecurityHeadersFilter filter = new SecurityHeadersFilter();

    private HttpServletRequest createMockRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/hello");
        return request;
    }

    @Test
    @DisplayName("Filter should add X-Content-Type-Options header")
    void filter_ShouldAdd_XContentTypeOptions() throws Exception {
        HttpServletRequest request = createMockRequest();
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(response).setHeader("X-Content-Type-Options", "nosniff");
    }

    @Test
    @DisplayName("Filter should add X-Frame-Options header")
    void filter_ShouldAdd_XFrameOptions() throws Exception {
        HttpServletRequest request = createMockRequest();
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(response).setHeader("X-Frame-Options", "DENY");
    }

    @Test
    @DisplayName("Filter should add Content-Security-Policy header")
    void filter_ShouldAdd_CSP() throws Exception {
        HttpServletRequest request = createMockRequest();
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(response).setHeader(eq("Content-Security-Policy"), anyString());
    }

    @Test
    @DisplayName("Filter should add Referrer-Policy header")
    void filter_ShouldAdd_ReferrerPolicy() throws Exception {
        HttpServletRequest request = createMockRequest();
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(response).setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    @Test
    @DisplayName("Filter should continue filter chain")
    void filter_ShouldContinueChain() throws Exception {
        HttpServletRequest request = createMockRequest();
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
