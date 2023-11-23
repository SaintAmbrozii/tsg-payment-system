package com.example.tsgpaymentsystem.service.exporters;

import com.example.tsgpaymentsystem.dto.PaymentDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class PrintedPage {

    private static String template;

    static {
        InputStream resourceAsStream = PrintedPage.class.getResourceAsStream("/templates/printable.html");
        if (resourceAsStream == null)
            log.error("PrintedPage cannot find the tamplate");
        else {
            try {
                template = StreamUtils.copyToString(resourceAsStream, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("Cannot read the template", e);
            }
        }
    }

    private List<PaymentDto> items;
    private String total;
    private String from;
    private String to;
    private String timestamp;
    private String contract;

    public String buildPage() {
        if (template == null)
            return "ERROR. Template cannot be found";

        // как по шаблону html
        return template.replace("{{from}}", from)
                .replace("{{to}}", to)
                .replace("{{itemsCount}}", String.valueOf(items.size()))
                .replace("{{total}}", total)
                .replace("{{items}}", buildItemsTable())
                .replace("{{contract}}", contract);
    }

    private String buildItemsTable() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < items.size(); i++)
            addTr(i + 1, items.get(i), stringBuilder);

        return stringBuilder.toString();
    }

    private void addTr(int number, PaymentDto item, StringBuilder s) {
        s.append("<tr>");
        s.append("<td>").append(number).append("</td>");
        s.append("<td>").append(item.getService()).append("</td>");
        s.append("<td>").append(item.getAddress()).append("</td>");
        s.append("<td>").append(item.getAccount()).append("</td>");
        s.append("<td>").append(item.getPayment()).append("</td>");
        s.append("<td>").append(item.getTimestamp()).append("</td>");
        s.append("</tr>");
    }
}
