package com.demetrius.blog.user.domain.user.entity;

import com.demetrius.blog.user.domain.user.valueobject.UserStatus;
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
    private String email;
    private String nickname;
    private String avatar;
    private UserStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
