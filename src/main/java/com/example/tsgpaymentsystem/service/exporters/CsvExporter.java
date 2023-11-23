package com.example.tsgpaymentsystem.service.exporters;

import com.example.tsgpaymentsystem.domain.Payment;
import com.example.tsgpaymentsystem.utils.DateUtils;
import com.example.tsgpaymentsystem.utils.MoneyUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class CsvExporter implements IExporter{

    private static final String NL = "\r\n";
    private static final String SEMI_COLON = ";";

    @Override
    public byte[] export(List<Payment> payments, boolean withDates) throws Exception {
        StringBuilder str = new StringBuilder();

        log.debug(">Сообщаем о странице{} платежек", payments.size());
        for (Payment item : payments) {
            buildCsvLine(str, item, withDates);
            str.append(NL);
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
             BufferedWriter out = new BufferedWriter(osw)) {
            out.write('\ufeff'); // BOM
            out.write(str.toString());
            out.flush();
            return bos.toByteArray();
        }
    }
    private void buildCsvLine(StringBuilder str, Payment payment, boolean withDate) {
        str.append(payment.getService().getService()) //капитальный ремонт
                .append(SEMI_COLON)
                .append(payment.getAddress().getBuilding().getBuilding()).append(", ").append(payment.getAddress().getApartment()) // улица, здание, номер.квартира
                .append(SEMI_COLON)
                .append(payment.getAccount().getAccount()) //номер аккаунта
                .append(SEMI_COLON)
                .append(MoneyUtils.formatRUNoGrouping(payment.getPayment())); //сколько платить
        if (withDate)
            str.append(SEMI_COLON)
                    .append(payment.getTimestamp().withZoneSameInstant(DateUtils.DEFAULT_ZONE).format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
}
