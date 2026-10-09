package com.posterm.mobiledashboard.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public final class TransactionPage {
    private List<Transaction> items;

    @SerializedName("next_cursor")
    private String nextCursor;

    public List<Transaction> getItems() {
        return items;
    }

    public String getNextCursor() {
        return nextCursor;
    }
}
