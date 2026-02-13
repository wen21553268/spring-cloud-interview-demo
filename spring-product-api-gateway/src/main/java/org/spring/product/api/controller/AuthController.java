package org.spring.product.api.controller;

import io.jsonwebtoken.Claims;
import org.spring.product.api.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.cors.CorsGatewayFilterApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String clientId = "Ov23lisr0a0UVxin0nhk";
    private static final String clientSecret  = "597b00e094b7170af6c4754eecdc6edfba728925";
    private static final String redirectUri = "http://localhost:7573/auth/github/callback";
    private static final List<String> ADMINISTRATES = List.of("PRODUCT_ADMIN", "EDITOR", "USER");
    private static final List<String> EDITORROLES = List.of("EDITOR", "USER");
    private static final List<String> USERROLES = List.of("USER");
    private static final String githubAuthUrl;

    static {
        githubAuthUrl = "https://github.com/login/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=user:email";
    }

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CorsGatewayFilterApplicationListener corsGatewayFilterApplicationListener;

    @PostMapping("/login")
    public Mono<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if ("user_1".equals(username) && "user_1".equals(password)) {
            return Mono.just(Map.of(
                    "token", jwtUtil.generateToken(username, USERROLES),
                    "username", username,
                    "roles", USERROLES)
            );
        } else if ("editor_1".equals(username) && "editor_1".equals(password)) {
            return Mono.just(Map.of(
                    "token", jwtUtil.generateToken(username, EDITORROLES),
                    "username", username,
                    "roles", EDITORROLES)
            );
        } else if ("adm_1".equals(username) && "adm_1".equals(password)) {
            return Mono.just(Map.of(
                    "token", jwtUtil.generateToken(username, ADMINISTRATES),
                    "username", username,
                    "roles", ADMINISTRATES)
            );
        }
        return Mono.error(new RuntimeException("用户名或密码错误"));
    }

    @GetMapping("/verify")
    public Mono<?> verifyToken(@RequestHeader("Authorization") String token) {
        boolean valid = jwtUtil.validateToken(token);
        if (valid) {
            Claims claims = jwtUtil.parseToken(token);
            return Mono.just(Map.of(
                    "valid", true,
                    "username", claims.getSubject(),
                    "roles", claims.get("roles")
            ));
        }
        return Mono.just(Map.of("valid", false));
    }

    @GetMapping("/github/login")
    public Mono<Void> githubLogin(ServerWebExchange exchange) {

        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
        exchange.getResponse().getHeaders().setLocation(URI.create(githubAuthUrl));
        return Mono.empty();
    }

    @GetMapping("/github/callback")
    public Mono<?> githubCallback(@RequestParam String code) {
        String tokenUrl = "https://github.com/login/oauth/access_token";

        return WebClient.create()
                .post()
                .uri(tokenUrl)
                .header("Accept", "application/json")
                .bodyValue(Map.of(
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "code", code,
                        "redirect_uri", redirectUri
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .doOnNext(tokenResponse -> System.out.println("=== Step2: token response: " + tokenResponse)) // 👈
                .flatMap(tokenResponse -> {
                    String accessToken = (String) tokenResponse.get("access_token");
                    System.out.println("=== Step3: accessToken: " + accessToken); // 👈

                    return WebClient.create()
                            .get()
                            .uri("https://api.github.com/user")
                            .header("Authorization", "Bearer " + accessToken)
                            .retrieve()
                            .bodyToMono(Map.class);
                })
                .doOnNext(userInfo -> System.out.println("=== Step4: user info: " + userInfo)) // 👈
                .flatMap(userInfo -> {
                    String login = (String) userInfo.get("login");
                    String email = (String) userInfo.get("email");
                    System.out.println("=== Step5: login=" + login + ", email=" + email); // 👈

                    List<String> roles;
                    List<String> simpleRoles;

                    List<String> adminUsers = List.of("wen21553268");
                    List<String> editorDomains = List.of("@company.com", "@example.com");

                    if (adminUsers.contains(login)) {
                        roles = ADMINISTRATES;
                        simpleRoles = ADMINISTRATES;
                    } else if (email != null && editorDomains.stream().anyMatch(email::endsWith)) {
                        roles = EDITORROLES;
                        simpleRoles = EDITORROLES;
                    } else {
                        roles = USERROLES;
                        simpleRoles = USERROLES;
                    }

                    String jwt = jwtUtil.generateToken(login, roles);
                    System.out.println("=== Step6: jwt generated: " + jwt); // 👈

                    Map<String, Object> result = new HashMap<>();
                    result.put("token", jwt);
                    result.put("username", login);
                    result.put("email", email);
                    result.put("roles", simpleRoles);
                    return Mono.just(result);
                });
    }

}