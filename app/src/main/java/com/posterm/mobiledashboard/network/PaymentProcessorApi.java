package com.posterm.mobiledashboard.network;

import com.posterm.mobiledashboard.model.TerminalListResponse;
import com.posterm.mobiledashboard.model.Transaction;
import com.posterm.mobiledashboard.model.TransactionPage;
import com.posterm.mobiledashboard.model.TransactionStatus;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

public interface PaymentProcessorApi {
    @GET("v1/transactions")
    Call<TransactionPage> listTransactions(
            @Query("terminal_id") String terminalId,
            @Query("status") List<TransactionStatus> statuses,
            @Query("transaction_id_prefix") String transactionIdPrefix,
            @Query("min_amount_minor") Long minAmountMinor,
            @Query("max_amount_minor") Long maxAmountMinor,
            @Query("currency") String currency,
            @Query("reason") String reason,
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
