package com.demetrius.blog.category.domain.category.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum CategoryStatus {

    ENABLED(1, "正常"),
    DISABLED(0, "禁用");

    private final int code;
    private final String desc;

    public static CategoryStatus of(int code) {
        for (CategoryStatus status : values()) {
            if (status.code == code) return status;
        }
        return DISABLED;
    }
}
