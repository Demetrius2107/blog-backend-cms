package com.demetrius.blog.user.domain.user.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public enum UserStatus {

    ENABLED(1, "正常"),
    DISABLED(0, "禁用");

    private final int code;
    private final String desc;

    public static UserStatus of(int code) {
        for (UserStatus status : values()) {
            if (status.code == code) return status;
        }
        return DISABLED;
    }
}
