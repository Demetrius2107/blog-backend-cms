package com.demetrius.blog.common.context;

import java.util.Set;

public class UserContext {

    private static final ThreadLocal<UserInfo> USER_HOLDER = new ThreadLocal<>();

    public static void set(UserInfo userInfo) {
        USER_HOLDER.set(userInfo);
    }

    public static UserInfo get() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        UserInfo info = get();
        return info != null ? info.getUserId() : null;
    }

    public static String getUsername() {
        UserInfo info = get();
        return info != null ? info.getUsername() : null;
    }

    public static Set<String> getRoles() {
        UserInfo info = get();
        return info != null ? info.getRoles() : Set.of();
    }

    public static void clear() {
        USER_HOLDER.remove();
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    @lombok.Builder
    public static class UserInfo {
        private Long userId;
        private String username;
        private Set<String> roles;
    }
}
