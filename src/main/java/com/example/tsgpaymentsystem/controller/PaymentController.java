package com.example.tsgpaymentsystem.controller;

import com.example.tsgpaymentsystem.domain.User;
import com.example.tsgpaymentsystem.dto.PaymentPage;
import com.example.tsgpaymentsystem.dto.seacrhcriteria.PaymentSearchCriteria;
import com.example.tsgpaymentsystem.service.PaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("api/payments/")
@SecurityRequirement(name = "JWT")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    @GetMapping
    public PaymentPage payments(@AuthenticationPrincipal User user,
                                @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                @RequestParam(value = "count", defaultValue = "10", required = false) int size,
                                @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
                                @RequestParam(value = "sort", defaultValue = "id", required = false) String sortProperty) {
        Sort sort = Sort.by(new Sort.Order(direction, sortProperty));
        Pageable pageable = PageRequest.of(page, size, sort);
        return PaymentPage.of(paymentService.findByUser(user, pageable));
    }

    @PostMapping("/filter")
    public PaymentPage filter(@AuthenticationPrincipal User user, @RequestBody PaymentSearchCriteria searchCriteria) {
        log.debug("PaymentSearchCriteria={}", searchCriteria);
        searchCriteria.validate();
        return PaymentPage.of(paymentService.findByCriteria(user, searchCriteria));
    }

    @PostMapping(value = "/export/csv", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    byte[] exportCsv(@AuthenticationPrincipal User user, @RequestBody PaymentSearchCriteria searchCriteria) throws Exception {
        log.debug(">>> exportCsv payments {}", searchCriteria);
        searchCriteria.validate();
        return paymentService.createExportCSVFile(user, searchCriteria);
    }

    @PostMapping(value = "/export/xls", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    byte[] exportXls(@AuthenticationPrincipal User user, @RequestBody PaymentSearchCriteria searchCriteria) throws Exception {
        log.debug(">>> exportXls payments {}", searchCriteria);
        searchCriteria.validate();
        return paymentService.createExportXLSFile(user, searchCriteria);
    }


    @PostMapping(value = "/export/print", produces = MediaType.TEXT_HTML_VALUE)
    public @ResponseBody
    String exportPrinted(@AuthenticationPrincipal User user, @RequestBody PaymentSearchCriteria searchCriteria) throws Exception {
        log.debug(">>> exportPrinted payments {}", searchCriteria);
        searchCriteria.validate();
        if (ObjectUtils.isEmpty(user.getContract()))
            throw new IllegalArgumentException("Добавьте информацию о реквизитах");

        return paymentService.createExportPrinted(user, searchCriteria);
    }
}
