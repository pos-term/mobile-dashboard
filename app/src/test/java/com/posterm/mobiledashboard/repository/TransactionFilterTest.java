package com.posterm.mobiledashboard.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.posterm.mobiledashboard.model.Transaction;
import com.posterm.mobiledashboard.model.TransactionStatus;

import org.junit.Test;

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

    private Transaction transaction(String terminalId, String status, String createdAt) {
        String json = "{"
                + "\"transaction_id\":\"tx-1\","
                + "\"terminal_id\":\"" + terminalId + "\","
                + "\"amount_minor\":1250,"
                + "\"currency\":\"RUB\","
                + "\"status\":\"" + status + "\","
                + "\"created_at\":\"" + createdAt + "\""
                + "}";
        return new Gson().fromJson(json, Transaction.class);
    }
}
