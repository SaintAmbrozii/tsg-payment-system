package com.example.tsgpaymentsystem.dto;

import com.example.tsgpaymentsystem.utils.MoneyUtils;
import lombok.Data;

import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PaymentPage {

    private long totalElements;
    private int totalPages;
    private int size;
    private int number;
    private int numberOfElements;
    private List<PaymentDto> content;
    private String totalSum;

    public static PaymentPage of(Page<PaymentDto> page) {
        PaymentPage result = new PaymentPage();
        result.setContent(page.getContent());
        result.setNumberOfElements(page.getNumberOfElements());
        result.setSize(page.getSize());
        result.setTotalElements(page.getTotalElements());
        result.setTotalPages(page.getTotalPages());
        result.setNumber(page.getNumber());

        int totalSum = 0;
        for (PaymentDto p : page.getContent())
            totalSum += p.getPaymentAmount() * 100;

        result.setTotalSum(MoneyUtils.formatRU(totalSum / 100d));

        return result;
    }
}
