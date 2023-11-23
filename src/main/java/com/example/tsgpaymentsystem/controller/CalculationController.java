package com.example.tsgpaymentsystem.controller;

import com.example.tsgpaymentsystem.domain.User;
import com.example.tsgpaymentsystem.dto.CalculationDto;
import com.example.tsgpaymentsystem.dto.seacrhcriteria.CalculationSearchCriteria;
import com.example.tsgpaymentsystem.service.CalculationService;
import com.example.tsgpaymentsystem.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/calculations/")
@CrossOrigin(maxAge = 3600)
@Slf4j
public class CalculationController {

    private final CalculationService calculationService;
    private final UploadService uploadService;

    public CalculationController(CalculationService calculationService, UploadService uploadService) {
        this.calculationService = calculationService;
        this.uploadService = uploadService;
    }
    @PostMapping
    public Page<CalculationDto> current(@AuthenticationPrincipal User user, @RequestBody CalculationSearchCriteria searchCriteria) {
        return calculationService.findCurrentList(user, searchCriteria.getPageable());
    }

    @PostMapping("/archive")
    public Page<CalculationDto> archive(@AuthenticationPrincipal User user, @RequestBody CalculationSearchCriteria searchCriteria) {
        searchCriteria.validate();
        searchCriteria.setSortProperty("lastUploadDate");
        searchCriteria.setDirection(Sort.Direction.DESC);
        return calculationService.findArchiveList(user, searchCriteria);
    }

    @PostMapping("/upload")
    public void handleFileUpload(@AuthenticationPrincipal User user,
                                 @RequestParam(value = "services") Long[] serviceIds,
                                 @RequestParam("files") MultipartFile[] files) throws Throwable {
        log.debug(">>> Upload {} files for service {}", files.length, serviceIds);
        uploadService.store(user, files, serviceIds);
    }
}
