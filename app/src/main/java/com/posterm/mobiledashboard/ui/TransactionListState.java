package com.posterm.mobiledashboard.ui;

import com.posterm.mobiledashboard.model.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TransactionListState {
    public enum Status {
        LOADING,
        CONTENT,
        EMPTY,
        ERROR
    }

    private final Status status;
    private final List<Transaction> transactions;
    private final boolean refreshing;
    private final boolean loadingMore;
    private final boolean hasPrevious;
    private final boolean hasMore;
    private final int pageNumber;
    private final String errorMessage;

    public TransactionListState(
            Status status,
            List<Transaction> transactions,
            boolean refreshing,
            boolean loadingMore,
            boolean hasPrevious,
            boolean hasMore,
            int pageNumber,
            String errorMessage
    ) {
        this.status = status;
        this.transactions = Collections.unmodifiableList(new ArrayList<>(transactions));
        this.refreshing = refreshing;
        this.loadingMore = loadingMore;
        this.hasPrevious = hasPrevious;
        this.hasMore = hasMore;
        this.pageNumber = pageNumber;
        this.errorMessage = errorMessage;
    }

    public Status getStatus() {
        return status;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public boolean isRefreshing() {
        return refreshing;
    }

    public boolean isLoadingMore() {
        return loadingMore;
    }

    public boolean hasPrevious() {
        return hasPrevious;
    }

    public boolean hasMore() {
        return hasMore;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
