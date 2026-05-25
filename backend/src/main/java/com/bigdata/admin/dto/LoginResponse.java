package com.bigdata.admin.dto;

/**
 * Login Response DTO
 */
public class LoginResponse {

    private String token;
    private Long expiration;
    private UserInfo userInfo;

    // Constructors
    public LoginResponse() {}

    public LoginResponse(String token, Long expiration, UserInfo userInfo) {
        this.token = token;
        this.expiration = expiration;
        this.userInfo = userInfo;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String nickname;
        private Integer role;
        private String roleName;

        // Constructors
        public UserInfo() {}

        public UserInfo(Long id, String username, String email, String nickname, Integer role, String roleName) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.nickname = nickname;
            this.role = role;
            this.roleName = roleName;
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public Integer getRole() {
            return role;
        }

        public void setRole(Integer role) {
            this.role = role;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }
    }
}
