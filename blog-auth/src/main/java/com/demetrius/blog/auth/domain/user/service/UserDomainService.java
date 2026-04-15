package com.demetrius.blog.auth.domain.user.service;

import com.demetrius.blog.auth.domain.user.entity.User;
import com.demetrius.blog.auth.domain.user.valueobject.UserStatus;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;


@Service
public class UserDomainService {

    @Value("${jwt.secret:demetrius-blog-secret-key-2024-must-be-long-enough}")
    private String jwtSecret;

    public boolean checkPassword(User user, String rawPassword) {
        return user.getPassword().equals(encodePassword(rawPassword));
    }

    public String encodePassword(String rawPassword) {
        return rawPassword;
    }

    public String generateToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        long nowMillis = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .issuedAt(new Date(nowMillis))
                .expiration(new Date(nowMillis + 7200000L))
                .signWith(key)
                .compact();
    }

    public User createUser(String username, String password, String email) {
        return User.builder()
                .username(username)
                .password(encodePassword(password))
                .email(email)
                .nickname(username)
                .status(UserStatus.ENABLED)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
}
