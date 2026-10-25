package com.posterm.mobiledashboard.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.posterm.mobiledashboard.model.Transaction;
import com.posterm.mobiledashboard.model.TransactionPage;
import com.posterm.mobiledashboard.repository.TransactionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TransactionListViewModel extends ViewModel {
    private final TransactionRepository repository;
    private final MutableLiveData<TransactionListState> state = new MutableLiveData<>();
    private final List<Transaction> transactions = new ArrayList<>();

    private String nextCursor;
    private boolean requestInFlight;

    public TransactionListViewModel(TransactionRepository repository) {
        this.repository = repository;
        loadFirstPage(false);
    }

    public LiveData<TransactionListState> getState() {
        return state;
    }

    public void refresh() {
        loadFirstPage(true);
    }

    public void retry() {
        loadFirstPage(false);
    }

    public void loadNextPage() {
        if (requestInFlight || nextCursor == null) {
            return;
        }

        requestInFlight = true;
        publish(TransactionListState.Status.CONTENT, false, true, null);
        String requestedCursor = nextCursor;
        repository.getTransactions(requestedCursor, new TransactionRepository.Callback() {
            @Override
            public void onSuccess(TransactionPage page) {
                requestInFlight = false;
                transactions.addAll(itemsOrEmpty(page));
                nextCursor = page == null ? null : page.getNextCursor();
                publish(currentContentStatus(), false, false, null);
            }

            @Override
            public void onError(Throwable error) {
                requestInFlight = false;
                publish(currentContentStatus(), false, false, messageOf(error));
            }
        });
    }

    private void loadFirstPage(boolean refreshing) {
        if (requestInFlight) {
            return;
        }

        requestInFlight = true;
        if (refreshing && !transactions.isEmpty()) {
            publish(TransactionListState.Status.CONTENT, true, false, null);
        } else {
            publish(TransactionListState.Status.LOADING, false, false, null);
        }

        repository.getTransactions(null, new TransactionRepository.Callback() {
            @Override
            public void onSuccess(TransactionPage page) {
                requestInFlight = false;
                transactions.clear();
                transactions.addAll(itemsOrEmpty(page));
                nextCursor = page == null ? null : page.getNextCursor();
                publish(currentContentStatus(), false, false, null);
            }

            @Override
            public void onError(Throwable error) {
                requestInFlight = false;
                if (transactions.isEmpty()) {
                    nextCursor = null;
                    publish(TransactionListState.Status.ERROR, false, false, messageOf(error));
                } else {
                    publish(TransactionListState.Status.CONTENT, false, false, messageOf(error));
                }
            }
        });
    }

    private List<Transaction> itemsOrEmpty(TransactionPage page) {
        if (page == null || page.getItems() == null) {
            return Collections.emptyList();
        }
        return page.getItems();
    }

    private TransactionListState.Status currentContentStatus() {
        return transactions.isEmpty()
                ? TransactionListState.Status.EMPTY
                : TransactionListState.Status.CONTENT;
    }

    private void publish(
            TransactionListState.Status status,
            boolean refreshing,
            boolean loadingMore,
            String errorMessage
    ) {
        state.setValue(new TransactionListState(
                status,
                transactions,
                refreshing,
                loadingMore,
                nextCursor != null,
                errorMessage
        ));
    }

    private String messageOf(Throwable error) {
        if (error == null || error.getMessage() == null || error.getMessage().trim().isEmpty()) {
            return "Unknown transaction loading error";
        }
        return error.getMessage();
    }
}
