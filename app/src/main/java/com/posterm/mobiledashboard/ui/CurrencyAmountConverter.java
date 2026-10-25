package com.posterm.mobiledashboard.ui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;

final class CurrencyAmountConverter {
    private CurrencyAmountConverter() {
    }

    static long toMinorUnits(String value, String currencyCode) {
        int fractionDigits = fractionDigits(currencyCode);
        String normalizedValue = value == null
                ? ""
                : value.trim().replace(',', '.');
        if (normalizedValue.isEmpty()) {
            throw new IllegalArgumentException("Amount must not be blank");
        }
        try {
            return new BigDecimal(normalizedValue)
                    .movePointRight(fractionDigits)
                    .setScale(0, RoundingMode.UNNECESSARY)
                    .longValueExact();
        } catch (ArithmeticException | NumberFormatException error) {
            throw new IllegalArgumentException("Invalid amount for currency", error);
        }
    }

    static String toMajorUnits(long amountMinor, String currencyCode) {
        int fractionDigits = fractionDigits(currencyCode);
        return BigDecimal.valueOf(amountMinor, fractionDigits).toPlainString();
    }

    private static int fractionDigits(String currencyCode) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency is required for amount filters");
        }
        try {
            int digits = Currency.getInstance(currencyCode.trim().toUpperCase(Locale.US))
                    .getDefaultFractionDigits();
            if (digits < 0) {
                throw new IllegalArgumentException("Currency has no fraction digit definition");
            }
            return digits;
        } catch (IllegalArgumentException error) {
            throw new IllegalArgumentException("Invalid currency code", error);
        }
    }
}
