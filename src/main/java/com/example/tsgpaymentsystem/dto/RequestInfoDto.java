package com.example.tsgpaymentsystem.dto;

import com.example.tsgpaymentsystem.exception.BadPaymentDataException;
import lombok.Data;
import lombok.ToString;

import org.springframework.util.ObjectUtils;


@Data
@ToString
public class RequestInfoDto {
    private String tsgId;
    private String service;
    private String account;
    private String provider;
    private String address;

    public void validate() {
        if (ObjectUtils.isEmpty(tsgId))
            throw new BadPaymentDataException("tsgId не может быть NULL");

        if (ObjectUtils.isEmpty(service))
            throw new IllegalArgumentException("service cannot be empty");

        if (ObjectUtils.isEmpty(account))
            throw new IllegalArgumentException("account cannot be empty");

        if (ObjectUtils.isEmpty(provider))
            throw new IllegalArgumentException("provider cannot be empty");
    }
}
