package com.example.tsgpaymentsystem.controller;

import com.example.tsgpaymentsystem.domain.User;
import com.example.tsgpaymentsystem.dto.InfoDto;
import com.example.tsgpaymentsystem.service.InfoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/info")
@SecurityRequirement(name = "JWT")
public class InfoController {

    private final InfoService infoService;

    public InfoController(InfoService infoService) {
        this.infoService = infoService;
    }
    @GetMapping
    public InfoDto info(@AuthenticationPrincipal User user) {
        return infoService.findInfoByUser(user);
    }
}
