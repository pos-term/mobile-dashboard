package com.posterm.mobiledashboard.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.posterm.mobiledashboard.model.TransactionPage;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public final class MockTransactionRepository implements TransactionRepository {
    private static final String FIRST_PAGE_ASSET = "transactions/page_1.json";
    private static final String SECOND_PAGE_ASSET = "transactions/page_2.json";
    private static final String SECOND_PAGE_CURSOR = "mock-page-2";

    private final Context context;
    private final Gson gson;
    private final Handler handler;
    private final long delayMillis;
    private final boolean forceError;

    public MockTransactionRepository(Context context, long delayMillis, boolean forceError) {
        this.context = context.getApplicationContext();
        this.gson = new Gson();
        this.handler = new Handler(Looper.getMainLooper());
        this.delayMillis = delayMillis;
        this.forceError = forceError;
    }

    @Override
    public void getTransactions(String cursor, Callback callback) {
        handler.postDelayed(() -> {
            if (forceError) {
                callback.onError(new IOException("Forced mock transaction error"));
                return;
            }

            String assetName;
            if (cursor == null) {
                assetName = FIRST_PAGE_ASSET;
            } else if (SECOND_PAGE_CURSOR.equals(cursor)) {
                assetName = SECOND_PAGE_ASSET;
            } else {
                callback.onError(new IOException("Unknown mock cursor: " + cursor));
                return;
            }

            try (InputStream stream = context.getAssets().open(assetName);
                 Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                TransactionPage page = gson.fromJson(reader, TransactionPage.class);
                callback.onSuccess(page);
            } catch (IOException | RuntimeException error) {
                callback.onError(error);
            }
        }, delayMillis);
    }
}
