package com.bigdata.admin.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration
 * Configures authentication and authorization rules with JWT-based stateless authentication
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain...");

        http
            // Disable CSRF for stateless API
            .csrf(csrf -> csrf.disable())

            // Configure CORS
            .cors(cors -> cors.configure(http))

            // Configure session management - stateless for JWT
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - health check and API documentation
                .requestMatchers(
                    "/api/health",
                    "/api/error",
                    "/api/auth/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/h2-console/**"
                ).permitAll()

                // All other requests require authentication
                .anyRequest().authenticated()
            )

            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            // Configure exception handling
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    log.warn("Unauthorized access attempt: {}", request.getRequestURI());
                    response.sendError(401, "Unauthorized");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    log.warn("Access denied: {}", request.getRequestURI());
                    response.sendError(403, "Forbidden");
                })
            );

        log.info("Security configuration completed. JWT issuer: {}, expiration: {}ms",
                jwtProperties.getIssuer(), jwtProperties.getExpiration());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
