package com.example.tsgpaymentsystem.service;


import com.example.tsgpaymentsystem.domain.PayAgent;
import com.example.tsgpaymentsystem.domain.User;
import com.example.tsgpaymentsystem.repository.PayAgentReposiroty;
import com.example.tsgpaymentsystem.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepo;
    private final PayAgentReposiroty payAgentReposiroty;


    public UserDetailService(UserRepository userRepo, PayAgentReposiroty payAgentReposiroty) {

        this.userRepo = userRepo;
        this.payAgentReposiroty = payAgentReposiroty;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        final Optional<User> user = userRepo.findByEmail(email);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("No user exists with this email.");
        } else {
            return user.orElseThrow();

        }

    }


    public UserDetails loadByAgentName(String email) throws UsernameNotFoundException {
        final Optional<PayAgent> user = payAgentReposiroty.findByEmail(email);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("No agent exists with this email.");
        } else {
            return user.orElseThrow();

        }

    }





}
