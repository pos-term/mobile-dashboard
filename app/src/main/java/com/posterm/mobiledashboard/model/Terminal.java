package com.posterm.mobiledashboard.model;

import com.google.gson.annotations.SerializedName;

public final class Terminal {
    @SerializedName("terminal_id")
    private String terminalId;

    @SerializedName("last_transaction_at")
    private String lastTransactionAt;

    public String getTerminalId() {
        return terminalId;
    }

    public String getLastTransactionAt() {
        return lastTransactionAt;
    }
}
