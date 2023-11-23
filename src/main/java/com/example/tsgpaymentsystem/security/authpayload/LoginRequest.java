package com.example.tsgpaymentsystem.security.authpayload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class LoginRequest {

    private String email;
    private String password;
}
