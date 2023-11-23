package com.example.tsgpaymentsystem.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class MoneyUtils {

    private static final Locale ru = new Locale("ru", "RU");
    private static final NumberFormat ruCurrencyFormatter = NumberFormat.getNumberInstance(ru);
    private static final NumberFormat ruCurrencyFormatterNoGrouping = NumberFormat.getNumberInstance(ru);

    static {
        ruCurrencyFormatter.setMinimumFractionDigits(2);
        ruCurrencyFormatterNoGrouping.setMinimumFractionDigits(2);
        ruCurrencyFormatterNoGrouping.setGroupingUsed(false);

    }

    public static String formatRU(Double amount) {
        if (amount == null)
            amount = 0d;

        return ruCurrencyFormatter.format(amount);
    }

    public static String formatRUNoGrouping(Double amount) {
        if (amount == null)
            amount = 0d;

        return ruCurrencyFormatterNoGrouping.format(amount);
    }
}
