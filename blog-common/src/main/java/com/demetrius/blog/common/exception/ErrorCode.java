package com.demetrius.blog.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    SYSTEM_ERROR(500, "系统内部错误"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_ALREADY_EXISTS(1004, "用户已存在"),

    TOKEN_EXPIRED(2001, "Token已过期"),
    TOKEN_INVALID(2002, "Token无效"),

    ARTICLE_NOT_FOUND(3001, "文章不存在"),
    ARTICLE_PUBLISHED(3002, "文章已发布，无法删除"),

    CATEGORY_NOT_FOUND(4001, "分类不存在"),
    CATEGORY_HAS_ARTICLE(4002, "分类下存在文章，无法删除"),

    COMMENT_NOT_FOUND(5001, "评论不存在");

    private final int code;
    private final String message;

    public BizException toException() {
        return new BizException(this.code, this.message);
    }
}
