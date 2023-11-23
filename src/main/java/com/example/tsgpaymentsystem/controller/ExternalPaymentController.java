package com.example.tsgpaymentsystem.controller;

import com.example.tsgpaymentsystem.domain.User;
import com.example.tsgpaymentsystem.dto.PaymentRecordDto;
import com.example.tsgpaymentsystem.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/payments")
@CrossOrigin(maxAge = 3600)
@Slf4j
public class ExternalPaymentController {

    private final PaymentService paymentService;

    public ExternalPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    @PostMapping(value = "/accept", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public PaymentRecordDto acceptPayment(@AuthenticationPrincipal User agent, @RequestBody PaymentRecordDto payment) {
        log.debug(">>>>>>>  Платёж получен в ТСЖ {}, инфо {}", agent, payment);

        try {
            payment.validate();
            paymentService.addPayments(agent, payment);
            return payment.ok();
        } catch (Throwable any) {
            log.error("Платеж не был получен", any);
            return payment.fail(any);
        }
    }
}
