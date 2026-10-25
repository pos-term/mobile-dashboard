package com.posterm.mobiledashboard.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.posterm.mobiledashboard.model.Transaction;
import com.posterm.mobiledashboard.model.TransactionPage;
import com.posterm.mobiledashboard.repository.TransactionFilter;
import com.posterm.mobiledashboard.repository.TransactionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TransactionListViewModel extends ViewModel {
    private final TransactionRepository repository;
    private final MutableLiveData<TransactionListState> state = new MutableLiveData<>();
    private final List<Transaction> transactions = new ArrayList<>();
    private final List<String> pageCursors = new ArrayList<>();

    private String nextCursor;
    private int currentPageIndex;
    private boolean requestInFlight;
    private int requestGeneration;
    private TransactionFilter currentFilter = TransactionFilter.empty();

    public TransactionListViewModel(TransactionRepository repository) {
        this.repository = repository;
        pageCursors.add(null);
        loadFirstPage(false);
    }

    public LiveData<TransactionListState> getState() {
        return state;
    }

    public TransactionFilter getCurrentFilter() {
        return currentFilter;
    }

    public void applyFilter(TransactionFilter filter) {
        TransactionFilter safeFilter = filter == null ? TransactionFilter.empty() : filter;
        if (safeFilter.equals(currentFilter)) {
            return;
        }

        requestGeneration++;
        requestInFlight = false;
        currentFilter = safeFilter;
        transactions.clear();
        nextCursor = null;
        currentPageIndex = 0;
        pageCursors.clear();
        pageCursors.add(null);
        loadFirstPage(false);
    }

    public void refresh() {
        loadPage(pageCursors.get(currentPageIndex), currentPageIndex, true, false);
    }

    public void retry() {
        loadFirstPage(false);
    }

    public void loadNextPage() {
        if (requestInFlight || nextCursor == null) {
            return;
        }

        String requestedCursor = nextCursor;
        int targetPageIndex = currentPageIndex + 1;
        rememberPageCursor(targetPageIndex, requestedCursor);
        loadPage(requestedCursor, targetPageIndex, false, true);
    }

    public void loadPreviousPage() {
        if (requestInFlight || currentPageIndex == 0) {
            return;
        }
        int targetPageIndex = currentPageIndex - 1;
        loadPage(pageCursors.get(targetPageIndex), targetPageIndex, false, true);
    }

    private void loadFirstPage(boolean refreshing) {
        loadPage(null, 0, refreshing, false);
    }

    private void loadPage(
            String cursor,
            int targetPageIndex,
            boolean refreshing,
            boolean paging
    ) {
        if (requestInFlight) {
            return;
        }

        requestInFlight = true;
        if (paging && !transactions.isEmpty()) {
            publish(TransactionListState.Status.CONTENT, false, true, null);
        } else if (refreshing && !transactions.isEmpty()) {
            publish(TransactionListState.Status.CONTENT, true, false, null);
        } else {
            publish(TransactionListState.Status.LOADING, false, false, null);
        }

        int generation = requestGeneration;
        repository.getTransactions(currentFilter, cursor, new TransactionRepository.Callback() {
            @Override
            public void onSuccess(TransactionPage page) {
                if (generation != requestGeneration) {
                    return;
                }
                requestInFlight = false;
                transactions.clear();
                transactions.addAll(itemsOrEmpty(page));
                nextCursor = page == null ? null : page.getNextCursor();
                currentPageIndex = targetPageIndex;
                publish(currentContentStatus(), false, false, null);
            }

            @Override
            public void onError(Throwable error) {
                if (generation != requestGeneration) {
                    return;
                }
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

    private void rememberPageCursor(int pageIndex, String cursor) {
        if (pageCursors.size() == pageIndex) {
            pageCursors.add(cursor);
        } else if (pageCursors.size() > pageIndex) {
            pageCursors.set(pageIndex, cursor);
        }
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
                currentPageIndex > 0,
                nextCursor != null,
                currentPageIndex + 1,
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
