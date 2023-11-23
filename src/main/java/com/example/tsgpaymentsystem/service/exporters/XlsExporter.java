package com.example.tsgpaymentsystem.service.exporters;

import com.example.tsgpaymentsystem.domain.Payment;
import com.example.tsgpaymentsystem.utils.DateUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.time.ZonedDateTime;
import java.util.List;

public class XlsExporter implements IExporter{


    @Override
    public byte[] export(List<Payment> payments, boolean withDates) throws Exception {
        Workbook wb = new HSSFWorkbook(); //создаем документ
        Sheet sheet = wb.createSheet(); //создаем лист

        for (int i = 0; i < payments.size(); i++) { //обходим список платежек
            Payment payment = payments.get(i);
            Row row = sheet.createRow(i); //создаем строки в листе
            createStringCell(wb, row, 0, payment.getService().getService()); //передаем в метод воркбук, строки, номер столбца, название от колони платежа
            createStringCell(wb, row, 1, payment.getAddress().getBuilding().getBuilding() + ", " + payment.getAddress().getApartment());
            createStringCell(wb, row, 2, payment.getAccount().getAccount());
            createCurrencyCell(wb, row, 3, payment.getPayment());
            if (withDates)
                createDateCell(wb, row, 4, payment.getTimestamp());
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            wb.write(bos);
            wb.close();
            return bos.toByteArray();
        }
    }
    private static void createStringCell(Workbook wb, Row row, int column, String val) {
        Cell cell = row.createCell(column); //создаем столбец по номеру колонки
        cell.setCellValue(val); //задаем значение ячейки как строка
        CellStyle cellStyle = wb.createCellStyle(); //задаем стиль ячейки
        cellStyle.setAlignment(HorizontalAlignment.RIGHT);
        cellStyle.setVerticalAlignment(VerticalAlignment.BOTTOM);
        cell.setCellStyle(cellStyle);
    }
    private void createCurrencyCell(Workbook wb, Row row, int column, Double payment) {
        Cell cell = row.createCell(column); //ячейку по как столбец по номеру
        cell.setCellValue(payment); //ее значение как дубль число
        CellStyle cellStyle = wb.createCellStyle(); //стиль
        cellStyle.setAlignment(HorizontalAlignment.RIGHT);
        cellStyle.setVerticalAlignment(VerticalAlignment.BOTTOM);
        cell.setCellStyle(cellStyle);
    }

    private void createDateCell(Workbook wb, Row row, int column, ZonedDateTime payment) {
        Cell cell = row.createCell(column); // создаем ячейку как столбец по номеру
        cell.setCellValue(payment.withZoneSameInstant(DateUtils.DEFAULT_ZONE).toLocalDate()); //задаем значение как время
        CellStyle cellStyle = wb.createCellStyle(); //задаем стиль
        cellStyle.setDataFormat((short) 0xe);
        cellStyle.setAlignment(HorizontalAlignment.RIGHT);
        cellStyle.setVerticalAlignment(VerticalAlignment.BOTTOM);
        cell.setCellStyle(cellStyle);
    }

}
