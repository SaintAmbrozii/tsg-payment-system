package com.example.tsgpaymentsystem.dto;

import com.example.tsgpaymentsystem.domain.Payment;
import com.example.tsgpaymentsystem.utils.DateUtils;
import com.example.tsgpaymentsystem.utils.MoneyUtils;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PaymentDto {

    private Long id;
    private String service;
    private String address;
    private String account;
    private String payment;
    private double paymentAmount;
    private String outstandingDebt;
    private String timestamp;

    public static PaymentDto of(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.id = payment.getId();
        dto.account = payment.getAccount().getAccount();
        dto.address = payment.getAddress().getBuilding().getBuilding() + ", " + payment.getAddress().getApartment();
        dto.service = payment.getService().getService();
        dto.timestamp = DateUtils.ddmmyyyy_hhmmssZ(payment.getTimestamp());
        dto.payment = MoneyUtils.formatRU(payment.getPayment());
        dto.setPaymentAmount(payment.getPayment());
        dto.outstandingDebt = MoneyUtils.formatRU(payment.getOutstandingDebt());
        return dto;
    }
}
