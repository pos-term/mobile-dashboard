package com.posterm.mobiledashboard.repository;

import android.content.Context;

import com.posterm.mobiledashboard.network.ApiConfiguration;
import com.posterm.mobiledashboard.network.PaymentProcessorClient;

public final class TransactionRepositoryProvider {
    private static final long MOCK_DELAY_MILLIS = 900L;
    private static final boolean MOCK_FORCE_ERROR = false;

    private TransactionRepositoryProvider() {
    }

    public static TransactionRepository create(Context context) {
        // Replace this line with createApiRepository() when the backend is enabled.
        return new MockTransactionRepository(context, MOCK_DELAY_MILLIS, MOCK_FORCE_ERROR);
    }

    private static TransactionRepository createApiRepository() {
        ApiConfiguration configuration = new ApiConfiguration(
                "http://10.0.2.2:8080/",
                "",
                true
        );
        return new ApiTransactionRepository(PaymentProcessorClient.create(configuration).getApi());
    }
}
