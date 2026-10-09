package com.posterm.mobiledashboard.model;

import com.google.gson.annotations.SerializedName;

public final class Transaction {
    @SerializedName("transaction_id")
    private String transactionId;

    @SerializedName("terminal_id")
    private String terminalId;

    @SerializedName("amount_minor")
    private long amountMinor;

    private String currency;
    private TransactionStatus status;
    private String reason;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("processed_at")
    private String processedAt;

    public String getTransactionId() {
        return transactionId;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public long getAmountMinor() {
        return amountMinor;
    }

    public String getCurrency() {
        return currency;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getProcessedAt() {
        return processedAt;
    }
}
