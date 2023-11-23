package com.example.tsgpaymentsystem.controller;

import com.example.tsgpaymentsystem.domain.User;
import com.example.tsgpaymentsystem.dto.InformationResponseDto;
import com.example.tsgpaymentsystem.dto.RequestInfoDto;
import com.example.tsgpaymentsystem.service.InfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/api/v2/info/")
public class ExternalInfoController {

    private final InfoService infoService;

    public ExternalInfoController(InfoService infoService) {
        this.infoService = infoService;
    }

    @PostMapping(value = "/get", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public InformationResponseDto get(@AuthenticationPrincipal User user, @RequestBody RequestInfoDto req) {
        log.debug(">>>>>>>  Request to retrieve info {} req {}", user, req);

        try {
            req.validate();
            return infoService.findInfoByUser(user, req);
        } catch (Throwable any) {
            log.error("Information cannot be retrieved. Request {}, error {}", req, any);
            return InformationResponseDto.err(any);
        }
    }
}
