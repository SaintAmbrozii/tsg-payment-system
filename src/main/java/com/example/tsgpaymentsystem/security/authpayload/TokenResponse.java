package com.example.tsgpaymentsystem.security.authpayload;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class TokenResponse {

    private String token;
    private String refreshToken;
    private final String type = "Bearer";
}
