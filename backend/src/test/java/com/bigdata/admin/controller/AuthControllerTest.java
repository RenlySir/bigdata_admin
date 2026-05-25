package com.bigdata.admin.controller;

import com.bigdata.admin.common.Result;
import com.bigdata.admin.config.CustomUserPrincipal;
import com.bigdata.admin.config.JwtTokenProvider;
import com.bigdata.admin.config.TokenBlacklist;
import com.bigdata.admin.entity.User;
import com.bigdata.admin.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Test
    void refreshToken_ShouldPreserveCurrentTokenAbsoluteExpiration() {
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        TokenBlacklist tokenBlacklist = mock(TokenBlacklist.class);
        AuthController controller = new AuthController(
                authenticationManager,
                jwtTokenProvider,
                userMapper,
                passwordEncoder,
                tokenBlacklist
        );
        CustomUserPrincipal principal = mock(CustomUserPrincipal.class);
        when(principal.getUserId()).thenReturn(7L);
        User user = new User();
        user.setId(7L);
        user.setUsername("alice");
        when(userMapper.selectById(7L)).thenReturn(user);
        when(tokenBlacklist.isBlacklisted("old-token")).thenReturn(false);
        when(jwtTokenProvider.canRefreshToken("old-token")).thenReturn(true);
        when(jwtTokenProvider.getTokenVersion("old-token")).thenReturn(10L);
        when(jwtTokenProvider.getAbsoluteExpiration("old-token")).thenReturn(123456789L);
        when(jwtTokenProvider.generateToken(7L, "alice", 11L, 123456789L)).thenReturn("new-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(60_000L);

        Result<Map<String, String>> result = controller.refreshToken(principal, "Bearer old-token");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("new-token", result.getData().get("token"));
        verify(jwtTokenProvider).generateToken(7L, "alice", 11L, 123456789L);
    }
}
