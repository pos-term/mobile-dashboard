package com.posterm.mobiledashboard.repository;

import androidx.annotation.NonNull;

import com.posterm.mobiledashboard.model.TransactionPage;
import com.posterm.mobiledashboard.network.PaymentProcessorApi;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public final class ApiTransactionRepository implements TransactionRepository {
    private static final int PAGE_SIZE = 20;

    private final PaymentProcessorApi api;

    public ApiTransactionRepository(PaymentProcessorApi api) {
        this.api = api;
    }

    @Override
    public void getTransactions(TransactionFilter filter, String cursor, Callback callback) {
        TransactionFilter safeFilter = filter == null ? TransactionFilter.empty() : filter;
        api.listTransactions(
                        safeFilter.getTerminalId(),
                        safeFilter.getStatus(),
                        safeFilter.getFromInclusive(),
                        safeFilter.getToExclusive(),
                        PAGE_SIZE,
                        cursor
                )
                .enqueue(new retrofit2.Callback<TransactionPage>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<TransactionPage> call,
                            @NonNull Response<TransactionPage> response
                    ) {
                        TransactionPage page = response.body();
                        if (response.isSuccessful() && page != null) {
                            callback.onSuccess(page);
                            return;
                        }
                        callback.onError(new IOException(
                                "Transaction request failed with HTTP " + response.code()
                        ));
                    }

                    @Override
                    public void onFailure(@NonNull Call<TransactionPage> call, @NonNull Throwable error) {
                        callback.onError(error);
                    }
                });
    }
}
