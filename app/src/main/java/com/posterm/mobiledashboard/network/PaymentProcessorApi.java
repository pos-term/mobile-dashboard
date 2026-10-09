package com.posterm.mobiledashboard.network;

import com.posterm.mobiledashboard.model.TerminalListResponse;
import com.posterm.mobiledashboard.model.Transaction;
import com.posterm.mobiledashboard.model.TransactionPage;
import com.posterm.mobiledashboard.model.TransactionStatus;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PaymentProcessorApi {
    @GET("v1/transactions")
    Call<TransactionPage> listTransactions(
            @Query("terminal_id") String terminalId,
            @Query("status") TransactionStatus status,
            @Query("from") String fromInclusive,
            @Query("to") String toExclusive,
            @Query("limit") Integer limit,
            @Query("cursor") String cursor
    );

    @GET("v1/transactions/{transaction_id}")
    Call<Transaction> getTransaction(@Path("transaction_id") String transactionId);

    @GET("v1/terminals")
    Call<TerminalListResponse> listTerminals();
}
