package com.example.tsgpaymentsystem.controller;

import com.example.tsgpaymentsystem.domain.User;
import com.example.tsgpaymentsystem.dto.InfoDto;
import com.example.tsgpaymentsystem.dto.OptionDto;
import com.example.tsgpaymentsystem.dto.SettingsDto;
import com.example.tsgpaymentsystem.dto.UserDTO;
import com.example.tsgpaymentsystem.service.InfoService;
import com.example.tsgpaymentsystem.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings/")
@SecurityRequirement(name = "JWT")
public class SettingsController {

    private final InfoService infoService;
    private final UserService userService;

    public SettingsController(InfoService infoService, UserService userService) {
        this.infoService = infoService;
        this.userService = userService;
    }
    @GetMapping()
    public SettingsDto info(@AuthenticationPrincipal User user) {
        InfoDto infoDto = infoService.findInfoByUser(user);
        UserDTO userDto = UserDTO.toDto(user);
        return new SettingsDto(userDto, infoDto);
    }

    @GetMapping("/services/defaults")
    public OptionDto[] defaultServices(@AuthenticationPrincipal User user) {
        return infoService.findDefaultServices(user);
    }

    @PostMapping("/update")
    public UserDTO update(@AuthenticationPrincipal User user,@RequestBody UserDTO userDTO) {
        return UserDTO.toDto(userService.updateSettingsFields(user,userDTO));
    }

}
