package com.posterm.mobiledashboard.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.posterm.mobiledashboard.model.Transaction;
import com.posterm.mobiledashboard.model.TransactionStatus;

import org.junit.Test;

import java.util.Arrays;

public final class TransactionFilterTest {
    @Test
    public void matchesTerminalAndStatusIgnoringTerminalCase() {
        Transaction transaction = transaction(
                "TERM-02",
                "failed",
                "2026-10-25T08:00:00.000Z"
        );

        assertTrue(new TransactionFilter(
                "term-02",
                TransactionStatus.FAILED,
                null,
                null
        ).matches(transaction));
        assertFalse(new TransactionFilter(
                "TERM-01",
                TransactionStatus.FAILED,
                null,
                null
        ).matches(transaction));
        assertFalse(new TransactionFilter(
                "TERM-02",
                TransactionStatus.SUCCESS,
                null,
                null
        ).matches(transaction));
    }

    @Test
    public void dateRangeIncludesFromAndExcludesTo() {
        TransactionFilter filter = new TransactionFilter(
                null,
                null,
                "2026-10-25T08:00:00.000Z",
                "2026-10-26T00:00:00.000Z"
        );

        assertTrue(filter.matches(transaction(
                "TERM-01",
                "success",
                "2026-10-25T08:00:00.000Z"
        )));
        assertFalse(filter.matches(transaction(
                "TERM-01",
                "success",
                "2026-10-26T00:00:00.000Z"
        )));
    }

    @Test
    public void activeFilterCountTreatsDateRangeAsOneFilter() {
        TransactionFilter filter = new TransactionFilter(
                "TERM-01",
                TransactionStatus.PENDING,
                "2026-10-25T00:00:00.000Z",
                "2026-10-26T00:00:00.000Z"
        );

        assertEquals(3, filter.activeFilterCount());
    }

    @Test
    public void matchesExtendedFiltersAndMultipleStatuses() {
        Transaction transaction = transaction(
                "tx-12345",
                "TERM-02",
                15_050L,
                "RUB",
                "failed",
                "LIMIT_EXCEEDED",
                "2026-10-25T08:00:00.000Z"
        );
        TransactionFilter filter = new TransactionFilter(
                null,
                Arrays.asList(TransactionStatus.PENDING, TransactionStatus.FAILED),
                "TX-123",
                15_000L,
                16_000L,
                "rub",
                "limit_exceeded",
                null,
                null
        );

        assertTrue(filter.matches(transaction));
        assertEquals("RUB", filter.getCurrency());
        assertEquals(5, filter.activeFilterCount());
    }

    @Test
    public void rejectsAmountOutsideInclusiveRange() {
        Transaction transaction = transaction(
                "tx-1",
                "TERM-01",
                1_250L,
                "RUB",
                "success",
                null,
                "2026-10-25T08:00:00.000Z"
        );

        assertFalse(new TransactionFilter(
                null,
                null,
                null,
                1_251L,
                null,
                null,
                null,
                null,
                null
        ).matches(transaction));
        assertFalse(new TransactionFilter(
                null,
                null,
                null,
                null,
                1_249L,
                null,
                null,
                null,
                null
        ).matches(transaction));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvertedAmountRange() {
        new TransactionFilter(
                null,
                null,
                null,
                2_000L,
                1_000L,
                null,
                null,
                null,
                null
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidCurrencyCode() {
        new TransactionFilter(
                null,
                null,
                null,
                null,
                null,
                "RU",
                null,
                null,
                null
        );
    }

    private Transaction transaction(String terminalId, String status, String createdAt) {
        return transaction(
                "tx-1",
                terminalId,
                1_250L,
                "RUB",
                status,
                null,
                createdAt
        );
    }

    private Transaction transaction(
            String transactionId,
            String terminalId,
            long amountMinor,
            String currency,
            String status,
            String reason,
            String createdAt
    ) {
        String reasonJson = reason == null ? "null" : "\"" + reason + "\"";
        String json = "{"
                + "\"transaction_id\":\"" + transactionId + "\","
                + "\"terminal_id\":\"" + terminalId + "\","
                + "\"amount_minor\":" + amountMinor + ","
                + "\"currency\":\"" + currency + "\","
                + "\"status\":\"" + status + "\","
                + "\"reason\":" + reasonJson + ","
                + "\"created_at\":\"" + createdAt + "\""
                + "}";
        return new Gson().fromJson(json, Transaction.class);
    }
}
