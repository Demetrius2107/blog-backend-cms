package com.demetrius.blog.common.exception;

import lombok.Getter;
import com.demetrius.blog.common.response.Result;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class BizException extends RuntimeException {

    private int code;
    private String message;

    public BizException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    public static Result<Void> toResult(BizException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }
}
