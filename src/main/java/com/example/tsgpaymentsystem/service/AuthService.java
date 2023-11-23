package com.example.tsgpaymentsystem.service;

import com.example.tsgpaymentsystem.domain.User;
import com.example.tsgpaymentsystem.security.JwtTokenGenerator;
import com.example.tsgpaymentsystem.security.authpayload.LoginRequest;
import com.example.tsgpaymentsystem.security.authpayload.TokenResponse;
import io.jsonwebtoken.Claims;
import jakarta.security.auth.message.AuthException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final JwtTokenGenerator tokenGenerator;

    private final UserDetailService userService;
    private final Map<String, String> refreshStorage = new HashMap<>();
    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthService(JwtTokenGenerator tokenGenerator, UserDetailService userService) {
        this.tokenGenerator = tokenGenerator;
        this.userService = userService;
    }


    public TokenResponse authenticate(@NonNull LoginRequest request) throws AuthException {

     var authentifitation= authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
     SecurityContextHolder.getContext().setAuthentication(authentifitation);


         User user = (User) userService.loadUserByUsername(request.getEmail());
  //       user= (User) authentifitation.getPrincipal();
        final String jwtToken = tokenGenerator.generateToken(user);
        final String refreshToken = tokenGenerator.generateRefreshToken(user);
        refreshStorage.put(user.getEmail(), refreshToken);
        return TokenResponse.builder().token(jwtToken).refreshToken(refreshToken).build();

    }

    public TokenResponse getRefreshToken(@NonNull String refreshToken) throws AuthException {
        final Claims claims = tokenGenerator.extractAllClaims(refreshToken);
        final String email = claims.getSubject();
        UserDetails userDetails = userService.loadUserByUsername(email);
        if (tokenGenerator.validateToken(refreshToken, userDetails)) {
            final String AuthorizeToken = refreshStorage.get(email);
            if (AuthorizeToken != null && AuthorizeToken.equals(refreshToken)) {
                User user = (User) userService.loadUserByUsername(email);
                final String jwtToken = tokenGenerator.generateToken(user);
                final String newRefreshToken = tokenGenerator.generateRefreshToken(user);
                refreshStorage.put(user.getEmail(), newRefreshToken);
                return TokenResponse.builder().token(jwtToken).refreshToken(newRefreshToken).build();

            }
        }

        throw new AuthException("Невалидный JWT токен");
    }

    public TokenResponse getToken(@NonNull String refreshToken) throws AuthException {
        final Claims claims = tokenGenerator.extractAllClaims(refreshToken);
        final String email = claims.getSubject();
        UserDetails userDetails = userService.loadUserByUsername(email);
        if (tokenGenerator.validateToken(refreshToken, userDetails)) {
            final String AuthorizeToken = refreshStorage.get(email);
            if (AuthorizeToken != null && AuthorizeToken.equals(refreshToken)) {
                User user = (User) userService.loadUserByUsername(email);
                final String token = tokenGenerator.generateToken(user);
                refreshStorage.put(token, null);
                return TokenResponse.builder().token(token).refreshToken(null).build();
            }
        }
        return TokenResponse.builder().token(null).refreshToken(null).build();
    }




}
