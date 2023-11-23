package com.example.tsgpaymentsystem.domain;

import lombok.RequiredArgsConstructor;


import org.springframework.security.core.GrantedAuthority;
@RequiredArgsConstructor

public enum Roles implements GrantedAuthority {
    USER("USER"),ADMIN("ADMIN"),AGENT("AGENT");

    private final String roles;

    @Override
    public String getAuthority() {
        return roles;
    }
}
