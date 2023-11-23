package com.example.tsgpaymentsystem.dto;

import com.example.tsgpaymentsystem.domain.Calculation;
import com.example.tsgpaymentsystem.utils.DateUtils;
import com.example.tsgpaymentsystem.utils.MoneyUtils;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;


@Data
@Slf4j
@ToString
public class CalculationDto {
    private Long id;
    private String service;
    private String address;
    private String account;
    private String debt;
    private String outstandingDebt;
    private String timestamp;

    public static CalculationDto of(Calculation item) {
        CalculationDto dto = new CalculationDto();
        dto.id = item.getId();
        dto.account = item.getAccount().getAccount();
        dto.address = item.getAddress().getBuilding().getBuilding() + ", " + item.getAddress().getApartment();
        dto.debt = MoneyUtils.formatRU(item.getDebt());
        dto.outstandingDebt = MoneyUtils.formatRU(item.getOutstandingDebt());
        dto.service = item.getService().getService();
        dto.timestamp = DateUtils.ddmmyyyy(item.getLastUploadDate());
        return dto;
    }
}
