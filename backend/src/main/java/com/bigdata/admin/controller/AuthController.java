package com.bigdata.admin.controller;

import com.bigdata.admin.common.Result;
import com.bigdata.admin.config.CustomUserPrincipal;
import com.bigdata.admin.config.JwtTokenProvider;
import com.bigdata.admin.dto.LoginRequest;
import com.bigdata.admin.dto.LoginResponse;
import com.bigdata.admin.dto.RegisterRequest;
import com.bigdata.admin.entity.User;
import com.bigdata.admin.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication operations")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Operation(summary = "User login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Login attempt for user: {}", request.getUsername());

            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
            User user = userMapper.selectById(principal.getUserId());

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

            // Build response
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getNickname(),
                    user.getRole(),
                    getRoleName(user.getRole())
            );

            LoginResponse response = new LoginResponse(
                    token,
                    jwtTokenProvider.getExpirationTime(),
                    userInfo
            );

            log.info("User logged in successfully: {}", request.getUsername());
            return Result.success("Login successful", response);

        } catch (Exception e) {
            log.warn("Login failed for user: {}", request.getUsername());
            return Result.error(401, "Invalid username or password");
        }
    }

    @PostMapping("/register")
    @Operation(summary = "User registration")
    @com.bigdata.admin.config.RateLimitAspect.SensitiveRateLimit
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            log.info("Registration attempt for user: {}", request.getUsername());

            // Check if username already exists
            User existingUser = userMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                            .eq(User::getUsername, request.getUsername())
            );

            if (existingUser != null) {
                return Result.error("Username already exists");
            }

            // Check if email already exists
            existingUser = userMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                            .eq(User::getEmail, request.getEmail())
            );

            if (existingUser != null) {
                return Result.error("Email already exists");
            }

            // Create new user
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
            user.setRole(0); // Default role: user
            user.setStatus(1); // Active

            userMapper.insert(user);

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

            // Build response
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getNickname(),
                    user.getRole(),
                    getRoleName(user.getRole())
            );

            LoginResponse response = new LoginResponse(
                    token,
                    jwtTokenProvider.getExpirationTime(),
                    userInfo
            );

            log.info("User registered successfully: {}", request.getUsername());
            return Result.success("Registration successful", response);

        } catch (Exception e) {
            log.error("Registration failed for user: {}", request.getUsername(), e);
            return Result.error("Registration failed: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user info")
    public Result<LoginResponse.UserInfo> getCurrentUser(@AuthenticationPrincipal CustomUserPrincipal principal) {
        if (principal == null) {
            return Result.error(401, "Not authenticated");
        }

        User user = userMapper.selectById(principal.getUserId());
        if (user == null) {
            return Result.error("User not found");
        }

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                getRoleName(user.getRole())
        );

        return Result.success(userInfo);
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout")
    public Result<Void> logout() {
        // In a stateless JWT setup, logout is handled client-side by removing the token
        // For additional security, you could implement a token blacklist
        return Result.success("Logout successful", null);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token")
    public Result<Map<String, String>> refreshToken(@AuthenticationPrincipal CustomUserPrincipal principal) {
        if (principal == null) {
            return Result.error(401, "Not authenticated");
        }

        User user = userMapper.selectById(principal.getUserId());
        if (user == null) {
            return Result.error("User not found");
        }

        String newToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        Map<String, String> response = new HashMap<>();
        response.put("token", newToken);
        response.put("expiration", String.valueOf(jwtTokenProvider.getExpirationTime()));

        return Result.success("Token refreshed successfully", response);
    }

    private String getRoleName(Integer role) {
        return switch (role) {
            case 2 -> "Super Admin";
            case 1 -> "Admin";
            default -> "User";
        };
    }
}
