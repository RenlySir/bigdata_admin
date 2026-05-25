package com.bigdata.admin.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WhenValidToken_ShouldSetCustomPrincipalAndUserIdAttribute() throws ServletException, IOException {
        JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
        TokenBlacklist tokenBlacklist = mock(TokenBlacklist.class);
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
        CustomUserPrincipal principal = mock(CustomUserPrincipal.class);
        when(principal.getUserId()).thenReturn(42L);
        when(principal.getAuthorities()).thenReturn(java.util.List.of());
        when(jwtTokenProvider.validateToken("token-1")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("token-1")).thenReturn(42L);
        when(jwtTokenProvider.getUsernameFromToken("token-1")).thenReturn("alice");
        when(tokenBlacklist.isBlacklisted("token-1")).thenReturn(false);
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(principal);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenProvider, tokenBlacklist, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertSame(principal, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertEquals(42L, request.getAttribute("userId"));
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WhenTokenBlacklisted_ShouldNotAuthenticate() throws ServletException, IOException {
        JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
        TokenBlacklist tokenBlacklist = mock(TokenBlacklist.class);
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
        when(jwtTokenProvider.validateToken("revoked-token")).thenReturn(true);
        when(tokenBlacklist.isBlacklisted("revoked-token")).thenReturn(true);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenProvider, tokenBlacklist, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer revoked-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(chain).doFilter(request, response);
    }
}
