package com.posterm.mobiledashboard.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.posterm.mobiledashboard.model.Transaction;
import com.posterm.mobiledashboard.model.TransactionPage;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class MockTransactionRepository implements TransactionRepository {
    private static final String FIRST_PAGE_ASSET = "transactions/page_1.json";
    private static final String SECOND_PAGE_ASSET = "transactions/page_2.json";
    private static final String CURSOR_PREFIX = "mock-offset-";
    private static final int PAGE_SIZE = 5;

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
    public void getTransactions(TransactionFilter filter, String cursor, Callback callback) {
        handler.postDelayed(() -> {
            if (forceError) {
                callback.onError(new IOException("Forced mock transaction error"));
                return;
            }

            try {
                TransactionFilter safeFilter = filter == null ? TransactionFilter.empty() : filter;
                List<Transaction> matchingTransactions = new ArrayList<>();
                for (Transaction transaction : readAllTransactions()) {
                    if (safeFilter.matches(transaction)) {
                        matchingTransactions.add(transaction);
                    }
                }

                int offset = parseOffset(cursor);
                if (offset > matchingTransactions.size()) {
                    throw new IOException("Mock cursor is outside the filtered result: " + cursor);
                }
                int end = Math.min(offset + PAGE_SIZE, matchingTransactions.size());
                String nextCursor = end < matchingTransactions.size()
                        ? CURSOR_PREFIX + end
                        : null;
                MockPage payload = new MockPage(
                        new ArrayList<>(matchingTransactions.subList(offset, end)),
                        nextCursor
                );
                callback.onSuccess(gson.fromJson(gson.toJson(payload), TransactionPage.class));
            } catch (IOException | RuntimeException error) {
                callback.onError(error);
            }
        }, delayMillis);
    }

    private List<Transaction> readAllTransactions() throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        addAssetTransactions(FIRST_PAGE_ASSET, transactions);
        addAssetTransactions(SECOND_PAGE_ASSET, transactions);
        return transactions;
    }

    private void addAssetTransactions(String assetName, List<Transaction> destination) throws IOException {
        try (InputStream stream = context.getAssets().open(assetName);
             Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            TransactionPage page = gson.fromJson(reader, TransactionPage.class);
            if (page != null && page.getItems() != null) {
                destination.addAll(page.getItems());
            }
        }
    }

    private int parseOffset(String cursor) throws IOException {
        if (cursor == null) {
            return 0;
        }
        if (!cursor.startsWith(CURSOR_PREFIX)) {
            throw new IOException("Unknown mock cursor: " + cursor);
        }
        try {
            int offset = Integer.parseInt(cursor.substring(CURSOR_PREFIX.length()));
            if (offset < 0) {
                throw new NumberFormatException("negative offset");
            }
            return offset;
        } catch (NumberFormatException error) {
            throw new IOException("Invalid mock cursor: " + cursor, error);
        }
    }

    private static final class MockPage {
        private final List<Transaction> items;

        @SerializedName("next_cursor")
        private final String nextCursor;

        private MockPage(List<Transaction> items, String nextCursor) {
            this.items = items;
            this.nextCursor = nextCursor;
        }
    }
}
