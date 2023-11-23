package com.example.tsgpaymentsystem.controller;

import com.example.tsgpaymentsystem.domain.User;
import com.example.tsgpaymentsystem.dto.UserDTO;
import com.example.tsgpaymentsystem.security.authpayload.LoginRequest;
import com.example.tsgpaymentsystem.security.authpayload.TokenRefreshRequest;
import com.example.tsgpaymentsystem.security.authpayload.TokenResponse;
import com.example.tsgpaymentsystem.service.AuthService;
import com.example.tsgpaymentsystem.service.UserService;
import jakarta.security.auth.message.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping(value = "login",produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenResponse> authenticate(
            @RequestBody LoginRequest request
    ) throws AuthException {
        log.info("юзер авторизировался" + request.getEmail());
        return ResponseEntity.ok(authService.authenticate(request));

    }
    @PostMapping("token")
    public ResponseEntity<TokenResponse> getNewToken(@RequestBody TokenRefreshRequest request) throws AuthException {
        final TokenResponse token = authService.getToken(request.getRefreshToken());
        return ResponseEntity.ok(token);
    }
    @PostMapping("refresh")
    public ResponseEntity<TokenResponse> getNewRefreshToken(@RequestBody TokenRefreshRequest request) throws AuthException {
        final TokenResponse token = authService.getRefreshToken(request.getRefreshToken());
        return ResponseEntity.ok(token);
    }
    @PostMapping("register")
    public ResponseEntity<User> register(@RequestBody UserDTO user) throws Exception {
        log.info("юзер зарегистрировался" +user);
        return ResponseEntity.ok(userService.createUser(user));
    }
}
