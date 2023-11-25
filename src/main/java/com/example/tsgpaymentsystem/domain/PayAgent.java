package com.example.tsgpaymentsystem.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "payAgent")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PayAgent implements UserDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotNull(message = "не должен быть пустым")
    @Size(min = 2, max = 100, message = "Не может быть больше 100")
    private String username;

    @NotNull(message = "не должен быть пустым")
    @Size(min = 6, max = 100, message = "Не может быть больше 20")
    @Column(unique = true,length = 20)
    private String phone;

    @NotNull(message = "не должен быть пустым")
    @Size(min = 6, max = 100, message = "Не может быть больше 100")
    @Column(unique = true)
    private String email;

    @NotNull(message = "не должен быть пустым")
    @Size(min = 6, max = 100, message = "Не может быть больше 100")
    private String password;

    private boolean active = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(Roles.AGENT.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return active;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }


}
