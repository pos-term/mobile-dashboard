package com.posterm.mobiledashboard.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class CurrencyAmountConverterTest {
    @Test
    public void rubUsesTwoFractionDigits() {
        assertEquals(100_000L, CurrencyAmountConverter.toMinorUnits("1000", "RUB"));
        assertEquals("1000.00", CurrencyAmountConverter.toMajorUnits(100_000L, "RUB"));
    }

    @Test
    public void supportsZeroAndThreeFractionDigitCurrencies() {
        assertEquals(1_000L, CurrencyAmountConverter.toMinorUnits("1000", "JPY"));
        assertEquals(1_250L, CurrencyAmountConverter.toMinorUnits("1,250", "KWD"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void amountRequiresCurrency() {
        CurrencyAmountConverter.toMinorUnits("10", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsTooManyFractionDigits() {
        CurrencyAmountConverter.toMinorUnits("10.001", "RUB");
    }
}
