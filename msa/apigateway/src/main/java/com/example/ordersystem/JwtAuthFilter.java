package com.example.ordersystem;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter {

    @Value("${jwt.secretKey}")
    private String secretKey;

    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/",
            "/index.html",
            "/login.html",
            "/signup.html",
            "/product-create.html",
            "/product-detail.html",
            "/css/",
            "/js/",
            "/images/",
            "/member/create",
            "/member/doLogin",
            "/member/refresh-token",
            "/product/list",
            "/product/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getRawPath();

        if (!path.startsWith("/member") && !path.startsWith("/product") && !path.startsWith("/ordering")
                && !path.startsWith("/member-service") && !path.startsWith("/product-service") && !path.startsWith("/ordering-service")) {
            return chain.filter(exchange);
        }

        boolean isPublic = path.equals("/")
                || path.equals("/index.html")
                || path.equals("/login.html")
                || path.equals("/signup.html")
                || path.equals("/product-create.html")
                || path.equals("/product-detail.html")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.equals("/member/create")
                || path.equals("/member/doLogin")
                || path.equals("/member/refresh-token")
                || path.equals("/product/list")
                || path.matches("^/product/\\d+$")
                || path.equals("/member-service/member/create")
                || path.equals("/member-service/member/doLogin")
                || path.equals("/member-service/member/refresh-token")
                || path.equals("/product-service/product/list")
                || path.matches("^/product-service/product/\\d+$");
        if (isPublic) {
            return chain.filter(exchange);
        }

        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        try {
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                throw new IllegalArgumentException("token 관련 예외 발생");
            }
            String token = bearerToken.substring(7);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(builder -> builder
                            .header("X-User-Id", userId)
                            .header("X-User-Role", "ROLE_" + role))
                    .build();

            return chain.filter(modifiedExchange);
        } catch (IllegalArgumentException | MalformedJwtException | ExpiredJwtException | SignatureException |
                 UnsupportedJwtException e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
