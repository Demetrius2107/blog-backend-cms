package com.demetrius.blog.auth.domain.user.entity;

import com.demetrius.blog.auth.domain.user.valueobject.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String username;
    private String password;
    private String email;
    private String nickname;
    private String avatar;
    private UserStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public boolean isEnabled() {
        return this.status == UserStatus.ENABLED;
    }

    public void disable() {
        this.status = UserStatus.DISABLED;
    }

    public void enable() {
        this.status = UserStatus.ENABLED;
    }
}
