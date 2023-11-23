package com.example.tsgpaymentsystem.dto;

import lombok.Data;

@Data
public class UserInfoDto {

    public OptionDto[] accounts;
    public OptionDto[] services;
    public OptionDto[] addresses;
    public String lastCalculationDay;
}
