package com.example.tsgpaymentsystem.service.processor;

import com.example.tsgpaymentsystem.utils.AddressRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.ObjectUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.Callable;

@Data
@AllArgsConstructor
public class CalculationDocumentProcessor implements Callable<CalculationDocumentProcessor.CalculationDocumentResult> {

    public static final String DELIMETER = ";";
    public static final Locale RU = new Locale("ru", "RU");
    public static final Charset CP_1251 = Charset.forName("cp1251");
    private byte[] content;
    private final String defaultService;

    private String normalize(String address) {
        return address
                .trim()
                .replaceAll("\t", "")
                .replaceAll("\n", "");
    }

    private String normalizeService(String service) {
        service = service != null ? service.trim() : null;
        return ObjectUtils.isEmpty(service) ? defaultService : service;
    }

    private void appendLineToDocumentIfNoErrors(String line, CalculationDocumentResult document, DecimalFormat formatter, int i) {

        // <статья>;<адрес>;<лицевой счёт>;<долг>
        // ;Новосибирск,Б.Богаткова 228-228; 1488;	282,22

        if (ObjectUtils.isEmpty(line))
            return;

        String[] items = line.split(DELIMETER);
        if (items.length != 4) {
            document.getErrors().add("Неправильный формат строки " + i + ", количество столбцов != 4: " + line);
            return;
        }
        String service = normalizeService(items[0]);
        String address = normalize(items[1]);
        if (ObjectUtils.isEmpty(address)) {
            document.getErrors().add("Адрес не может быть пустым, см. строку " + i + ": " + line);
            return;
        }

        String account = normalize(items[2]);
        if (ObjectUtils.isEmpty(account)) {
            document.getErrors().add("Аккаунт не может быть пустым, см. строку " + i + ": " + line);
            return;
        }

        AddressRecord addressRecord;
        try {
            addressRecord = AddressRecord.createAddressRecord(address);
        } catch (Throwable any) {
            document.getErrors().add("Невозможно удалить аккаунт из адреса " + i + ": " + line);
            return;
        }

        double debt;
        try {
            debt = formatter.parse(normalize(items[3])).doubleValue();
        } catch (Throwable any) {
            document.getErrors().add("Невозможно распознать сумму " + i + ": " + line);
            return;
        }

        AccountRecord accountRecord = new AccountRecord(account, addressRecord);
        CalculationRecord record = createCalculationRecord(i, service, accountRecord, debt);
        document.getLines().add(record);
        document.getAccounts().add(accountRecord);
        document.getAddresses().add(addressRecord);
        document.getServices().add(service);
    }

    private CalculationRecord createCalculationRecord(int i, String service, AccountRecord account, Double debt) {
        return new CalculationRecord(i, service, account.getAddress(), account, debt);
    }

    private List<String> readAllLines(byte[] content) throws IOException {
        List<String> lines = new ArrayList<>();
        try (ByteArrayInputStream bytes = new ByteArrayInputStream(content);
             InputStreamReader reader = new InputStreamReader(bytes, CP_1251); // настройки кодировки
             BufferedReader bufferedReader = new BufferedReader(reader)) {

            String line;
            while ((line = bufferedReader.readLine()) != null)
                lines.add(line);
        }
        return lines; //возвращает строки из байткода
    }

    @Override
    public CalculationDocumentResult call() throws Exception {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getNumberInstance(RU);

        CalculationDocumentResult document = new CalculationDocumentResult(defaultService);
        List<String> lines = readAllLines(content);
        int lineNumber = 0;
        for (String line : lines)
            appendLineToDocumentIfNoErrors(line, document, formatter, lineNumber++);

        return document;
    }

    @Data
    public static class CalculationDocumentResult { //готовый документ для возврата
        private final String defaultService;
        private final List<CalculationRecord> lines = new ArrayList<>();
        private final Set<AddressRecord> addresses = new HashSet<>();
        private final Set<String> services = new HashSet<>();
        private final Set<AccountRecord> accounts = new HashSet<>();
        private final List<String> errors = new ArrayList<>();
    }

    @Data
    public static class AccountRecord { //отдает строку аккаунта и адрес
        private final String account;
        private final AddressRecord address;
    }

    @Data
    public static class CalculationRecord { //запись рассчётов
        private final int line;
        private final String service;
        private final AddressRecord address;
        private final AccountRecord account;
        private final double debt;
    }

}

