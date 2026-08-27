package com.demetrius.vellastra.gateway.filter;

import com.demetrius.vellastra.common.constant.BlogConstant;
import com.demetrius.vellastra.common.service.TokenBlackListService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * <h3>网关鉴权全局过滤器</h3>
 *
 * <p>职责：
 * <ol>
 *   <li>白名单路径直接放行</li>
 *   <li>校验 JWT Token 有效性</li>
 *   <li>解析 JWT 提取 userId/username/roles → 注入请求头 X-User-Id / X-Username / X-Roles</li>
 *   <li>非白名单且无有效 Token 返回 401</li>
 * </ol>
 * </p>
 *
 * @author wanqiu
 * @version 1.1
 * @since 2026-07-18
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final TokenBlackListService tokenBlackListService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${jwt.secret:demetrius-vellastra-secret-key-2024-must-be-long-enough}")
    private String jwtSecret;

    @Value("${gateway.white-list:/auth/login,/auth/register,/actuator/**,/doc.html,/v3/api-docs,/swagger-ui/**,/webjars/**}")
    private String whiteListConfig;

    public AuthGlobalFilter(TokenBlackListService tokenBlackListService) {
        this.tokenBlackListService = tokenBlackListService;
    }

    private List<String> getWhiteList() {
        return Arrays.asList(whiteListConfig.split(","));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单路径直接放行
        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }

        // 获取 Token
        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token == null || !token.startsWith(BlogConstant.TOKEN_PREFIX)) {
            return unauthorized(exchange.getResponse(), "未认证，请先登录");
        }

        // 去除 "Bearer " 前缀
        String jwtToken = token.substring(BlogConstant.TOKEN_PREFIX.length());

        try {
            // 解析 JWT
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(jwtToken)
                    .getPayload();

            // 检查 Token 是否在黑名单中（已登出）
            if (tokenBlackListService.isBlacklisted(jwtToken)) {
                return unauthorized(exchange.getResponse(), "Token 已登出，请重新登录");
            }

            String userId = claims.getSubject();
            String username = claims.get("username", String.class);
            String roles = claims.get("roles", String.class);

            // 将用户信息写入请求头，传递给下游微服务
            // 注意：必须先移除客户端可能伪造的 X-User-Id/X-Username/X-Roles 头，
            // 否则 header() 是追加而非覆盖，客户端伪造的值会与网关注入的值并存，
            // 下游 anyMatch("1"::equals) 会被绕过导致越权。
            ServerHttpRequest modifiedRequest = request.mutate()
                    .headers(headers -> {
                        headers.remove("X-User-Id");
                        headers.remove("X-Username");
                        headers.remove("X-Roles");
                    })
                    .header("X-User-Id", userId)
                    .header("X-Username", username != null ? username : "")
                    .header("X-Roles", roles != null ? roles : "")
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return unauthorized(exchange.getResponse(), "Token 已过期，请重新登录");
        } catch (io.jsonwebtoken.JwtException e) {
            return unauthorized(exchange.getResponse(), "Token 无效，请重新登录");
        } catch (Exception e) {
            return unauthorized(exchange.getResponse(), "认证失败");
        }
    }

    private boolean isWhiteListed(String path) {
        return getWhiteList().stream().map(String::trim)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String body = String.format("{\"code\":401,\"message\":\"%s\",\"data\":null}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}