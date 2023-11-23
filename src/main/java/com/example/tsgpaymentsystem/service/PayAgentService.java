package com.example.tsgpaymentsystem.service;

import com.example.tsgpaymentsystem.dto.PayAgentDto;
import com.example.tsgpaymentsystem.repository.PayAgentReposiroty;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PayAgentService  {

    public  final String DEFAULT_AGENT_EMAIL = "tsg-agent@mail.ru";

    private final PayAgentReposiroty payAgentReposiroty;

    public PayAgentService(PayAgentReposiroty payAgentReposiroty) {
        this.payAgentReposiroty = payAgentReposiroty;
    }


    public PayAgentDto findAgent() {
        return PayAgentDto.toDto(payAgentReposiroty.findByEmail(DEFAULT_AGENT_EMAIL).orElseThrow());
    }
}
