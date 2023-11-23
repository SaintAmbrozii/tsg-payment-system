package com.example.tsgpaymentsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InfoDto {

    public OptionDto[] accounts;
    public OptionDto[] services;
    public OptionDto[] addresses;

    public String lastCalculationDay;
}
