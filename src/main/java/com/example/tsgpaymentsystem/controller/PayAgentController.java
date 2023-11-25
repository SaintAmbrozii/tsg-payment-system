package com.example.tsgpaymentsystem.controller;

import com.example.tsgpaymentsystem.dto.PayAgentDto;
import com.example.tsgpaymentsystem.service.PayAgentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/api/agents/")
@Slf4j
@SecurityRequirement(name = "JWT")
public class PayAgentController {

    private final PayAgentService payAgentService;

    public PayAgentController(PayAgentService payAgentService) {
        this.payAgentService = payAgentService;
    }

    @GetMapping
    public PayAgentDto getDefaultAgent() {
        return payAgentService.findAgent();
    }
}
