package com.posterm.mobiledashboard.model;

import com.google.gson.annotations.SerializedName;

public enum TransactionStatus {
    @SerializedName("pending")
    PENDING("pending"),
    @SerializedName("success")
    SUCCESS("success"),
    @SerializedName("failed")
    FAILED("failed");

    private final String apiValue;

    TransactionStatus(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String toString() {
        return apiValue;
    }
}
