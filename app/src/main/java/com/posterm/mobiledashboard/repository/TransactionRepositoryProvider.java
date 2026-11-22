package com.posterm.mobiledashboard.repository;

import android.content.Context;

import com.posterm.mobiledashboard.BuildConfig;
import com.posterm.mobiledashboard.R;
import com.posterm.mobiledashboard.network.ApiConfiguration;
import com.posterm.mobiledashboard.network.PaymentProcessorClient;

public final class TransactionRepositoryProvider {
    private static final long MOCK_DELAY_MILLIS = 900L;

    private TransactionRepositoryProvider() {
    }

    public static TransactionRepository create(Context context) {
        if (BuildConfig.TRANSACTION_DEMO_MODE) {
            return new MockTransactionRepository(context, MOCK_DELAY_MILLIS, false);
        }

        try {
            ApiConfiguration configuration = new ApiConfiguration(
                    BuildConfig.PAYMENT_API_URL,
                    BuildConfig.PAYMENT_API_TOKEN
            );
            return new ApiTransactionRepository(PaymentProcessorClient.create(configuration).getApi());
        } catch (IllegalArgumentException error) {
            String message = context.getString(R.string.transactions_api_configuration_error);
            return (filter, cursor, callback) -> callback.onError(new IllegalStateException(message));
        }
    }
}
