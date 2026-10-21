package com.rahimtulla.smartpantry.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Small helpers for showing quantities and dates consistently across screens.
 */
public final class Formatters {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault());

    private Formatters() {
    }

    /** 2.0 -> "2", 0.5 -> "0.5", 1.23456 -> "1.235". */
    public static String quantity(double value) {
        return BigDecimal.valueOf(value)
                .setScale(3, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    /** "500 g", "2 pcs". */
    public static String amount(double quantity, String unit) {
        return quantity(quantity) + " " + unit;
    }

    public static long todayEpochDay() {
        return LocalDate.now().toEpochDay();
    }

    public static String date(long epochDay) {
        return LocalDate.ofEpochDay(epochDay).format(DATE);
    }
}
