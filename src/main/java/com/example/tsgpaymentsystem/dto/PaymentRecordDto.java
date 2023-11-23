package com.example.tsgpaymentsystem.dto;

import com.example.tsgpaymentsystem.exception.BadPaymentDataException;
import lombok.Data;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class PaymentRecordDto {
    private String tsgId;
    private Long paymentId;
    private Integer status;
    private String message;
    private List<Record> payments = new ArrayList<>();

    public void validate() {
        if (StringUtils.isEmpty(tsgId))
            throw new BadPaymentDataException("tsgId не может быть NULL");

        if (paymentId == null)
            throw new BadPaymentDataException("paymentId не может быть NULL");

        if (payments == null || payments.isEmpty())
            throw new BadPaymentDataException("В запросе отсутсвуют платежи");

        for (Record record : payments) {
            if (StringUtils.isEmpty(record.service))
                throw new BadPaymentDataException("service");

            if (StringUtils.isEmpty(record.account))
                throw new BadPaymentDataException("account");

            if (paymentId == null)
                throw new BadPaymentDataException("paymentId");

            if (record.amount == null || record.amount < 0f || record.amount > 100_000f)
                throw new BadPaymentDataException("amount");

            if (StringUtils.isEmpty(record.timestamp))
                throw new BadPaymentDataException("timestamp");

            //  if (StringUtils.isEmpty(address))
            //     throw new BadPaymentDataException("address");

        }
    }

    public PaymentRecordDto ok() {
        status = 200;
        message = "ok";
        payments = Collections.emptyList();
        return this;
    }

    public PaymentRecordDto fail(Throwable any) {
        status = 500;
        message = any != null ? any.getLocalizedMessage() : "Неизвестная ошибка:" + toString();
        payments = Collections.emptyList();
        return this;
    }

    @Data
    @ToString
    public static class Record {
        private String service;
        private String provider;
        private String address;
        private String account;
        private Double amount;
        private String timestamp;
    }
}
