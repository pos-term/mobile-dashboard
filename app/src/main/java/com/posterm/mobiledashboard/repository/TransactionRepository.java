package com.posterm.mobiledashboard.repository;

import com.posterm.mobiledashboard.model.TransactionPage;

public interface TransactionRepository {
    void getTransactions(String cursor, Callback callback);

    interface Callback {
        void onSuccess(TransactionPage page);

        void onError(Throwable error);
    }
}
