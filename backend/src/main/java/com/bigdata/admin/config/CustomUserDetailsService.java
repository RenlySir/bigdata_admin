package com.bigdata.admin.config;

import com.bigdata.admin.entity.User;
import com.bigdata.admin.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom User Details Service for Spring Security
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserMapper userMapper;

    public CustomUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);

        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .eq(User::getDeleted, 0)
        );

        if (user == null) {
            log.warn("User not found: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }

        if (user.getStatus() == 0) {
            log.warn("User account is disabled: {}", username);
            throw new UsernameNotFoundException("User account is disabled: " + username);
        }

        // Build authorities based on role
        var authorities = Collections.singletonList(
                new SimpleGrantedAuthority(getRoleName(user.getRole()))
        );

        log.debug("User loaded successfully: {}", username);
        return new CustomUserPrincipal(user, authorities);
    }

    private String getRoleName(Integer role) {
        return switch (role) {
            case 2 -> "ROLE_SUPER_ADMIN";
            case 1 -> "ROLE_ADMIN";
            default -> "ROLE_USER";
        };
    }
}
