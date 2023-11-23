package com.example.tsgpaymentsystem.service;

import com.example.tsgpaymentsystem.domain.Payment;
import com.example.tsgpaymentsystem.dto.PaymentDto;
import com.example.tsgpaymentsystem.dto.seacrhcriteria.PaymentSearchCriteria;
import com.example.tsgpaymentsystem.service.exporters.ExporterFactory;
import com.example.tsgpaymentsystem.service.exporters.PrintedPage;
import com.example.tsgpaymentsystem.utils.DateUtils;
import com.example.tsgpaymentsystem.utils.MoneyUtils;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class ReportService {

    public byte[] composeCSV(String contract, List<Payment> payments, boolean withDates) throws Exception {
        if (payments == null || payments.isEmpty())
            return new byte[0];

        return ExporterFactory.getCsvExporter().export(payments, withDates);

    }

    public byte[] composeXLS(String contract, List<Payment> payments, boolean withDates) throws Exception {
        if (payments == null || payments.isEmpty())
            return new byte[0];

        return ExporterFactory.getXlsExporter().export(payments, withDates);
    }

    public String composePrintable(String contract, List<Payment> paymentList, PaymentSearchCriteria searchCriteria) {
        PrintedPage printedPage = new PrintedPage();

        int totalSum = 0;
        for (Payment p : paymentList)
            totalSum += p.getPayment() * 100;

        printedPage.setContract(contract);
        printedPage.setTimestamp(DateUtils.ddmmyyyy_hhmmssZ(ZonedDateTime.now()));
        printedPage.setTotal(MoneyUtils.formatRU(totalSum / 100d));
        printedPage.setFrom(DateUtils.ddmmyyyy_hhmmssZ(searchCriteria.getFrom()));
        printedPage.setTo(DateUtils.ddmmyyyy_hhmmssZ(searchCriteria.getTo()));
        printedPage.setItems(paymentList.stream().map(PaymentDto::of).collect(Collectors.toList()));

        return printedPage.buildPage();
    }
}
