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


    // ID
    private Long id;

    // 用户名
    private String username;

    // 密码
    private String password;

    // 邮箱
    private String email;

    // 昵称
    private String nickname;

    // 头像
    private String avatar;

    // 状态
    private UserStatus status;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;

    // 判断是否启用
    public boolean isEnabled() {
        return this.status == UserStatus.ENABLED;
    }

    // 禁用
    public void disable() {
        this.status = UserStatus.DISABLED;
    }

    // 启用
    public void enable() {
        this.status = UserStatus.ENABLED;
    }
}
