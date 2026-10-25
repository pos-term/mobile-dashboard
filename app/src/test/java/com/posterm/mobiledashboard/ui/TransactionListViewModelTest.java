package com.posterm.mobiledashboard.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.google.gson.Gson;
import com.posterm.mobiledashboard.model.TransactionPage;
import com.posterm.mobiledashboard.model.TransactionStatus;
import com.posterm.mobiledashboard.repository.TransactionFilter;
import com.posterm.mobiledashboard.repository.TransactionRepository;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public final class TransactionListViewModelTest {
    @Rule
    public final InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void loadShowsLoadingThenContent() {
        FakeRepository repository = new FakeRepository(false);
        TransactionListViewModel viewModel = new TransactionListViewModel(repository);

        assertEquals(TransactionListState.Status.LOADING, stateOf(viewModel).getStatus());

        repository.succeed(page("tx-1", null));

        TransactionListState state = stateOf(viewModel);
        assertEquals(TransactionListState.Status.CONTENT, state.getStatus());
        assertEquals(1, state.getTransactions().size());
    }

    @Test
    public void emptyPageShowsEmptyState() {
        FakeRepository repository = new FakeRepository(true);
        repository.enqueueSuccess(emptyPage());

        TransactionListViewModel viewModel = new TransactionListViewModel(repository);

        assertEquals(TransactionListState.Status.EMPTY, stateOf(viewModel).getStatus());
        assertTrue(stateOf(viewModel).getTransactions().isEmpty());
    }

    @Test
    public void failureShowsErrorState() {
        FakeRepository repository = new FakeRepository(true);
        repository.enqueueError(new IOException("offline"));

        TransactionListViewModel viewModel = new TransactionListViewModel(repository);

        assertEquals(TransactionListState.Status.ERROR, stateOf(viewModel).getStatus());
        assertEquals("offline", stateOf(viewModel).getErrorMessage());
    }

    @Test
    public void retryLoadsContentAfterFailure() {
        FakeRepository repository = new FakeRepository(true);
        repository.enqueueError(new IOException("temporary"));
        repository.enqueueSuccess(page("tx-retry", null));
        TransactionListViewModel viewModel = new TransactionListViewModel(repository);

        viewModel.retry();

        assertEquals(TransactionListState.Status.CONTENT, stateOf(viewModel).getStatus());
        assertEquals("tx-retry", stateOf(viewModel).getTransactions().get(0).getTransactionId());
        assertEquals(2, repository.cursors.size());
    }

    @Test
    public void paginationAppendsNextPageAndUsesCursor() {
        FakeRepository repository = new FakeRepository(true);
        repository.enqueueSuccess(page("tx-1", "cursor-2"));
        repository.enqueueSuccess(page("tx-2", null));
        TransactionListViewModel viewModel = new TransactionListViewModel(repository);

        assertTrue(stateOf(viewModel).hasMore());
        viewModel.loadNextPage();

        TransactionListState state = stateOf(viewModel);
        assertEquals(Arrays.asList(null, "cursor-2"), repository.cursors);
        assertEquals(2, state.getTransactions().size());
        assertEquals("tx-2", state.getTransactions().get(1).getTransactionId());
        assertFalse(state.hasMore());
        assertFalse(state.isLoadingMore());
    }

    @Test
    public void applyingFilterReloadsFirstPageWithSelectedValues() {
        FakeRepository repository = new FakeRepository(true);
        repository.enqueueSuccess(page("tx-all", "cursor-old"));
        repository.enqueueSuccess(page("tx-filtered", null));
        TransactionListViewModel viewModel = new TransactionListViewModel(repository);
        TransactionFilter filter = new TransactionFilter(
                "TERM-02",
                TransactionStatus.FAILED,
                "2026-10-24T00:00:00.000Z",
                "2026-10-26T00:00:00.000Z"
        );

        viewModel.applyFilter(filter);

        assertEquals(filter, viewModel.getCurrentFilter());
        assertEquals(filter, repository.filters.get(1));
        assertEquals(Arrays.asList(null, null), repository.cursors);
        assertEquals(1, stateOf(viewModel).getTransactions().size());
        assertEquals("tx-filtered", stateOf(viewModel).getTransactions().get(0).getTransactionId());
    }

    private TransactionListState stateOf(TransactionListViewModel viewModel) {
        TransactionListState state = viewModel.getState().getValue();
        assertNotNull(state);
        return state;
    }

    private TransactionPage emptyPage() {
        return new Gson().fromJson("{\"items\":[],\"next_cursor\":null}", TransactionPage.class);
    }

    private TransactionPage page(String id, String nextCursor) {
        String cursorJson = nextCursor == null ? "null" : "\"" + nextCursor + "\"";
        String json = "{\"items\":[{"
                + "\"transaction_id\":\"" + id + "\","
                + "\"terminal_id\":\"TERM-01\","
                + "\"amount_minor\":1250,"
                + "\"currency\":\"RUB\","
                + "\"status\":\"success\","
                + "\"created_at\":\"2026-10-25T08:00:00.000Z\""
                + "}],\"next_cursor\":" + cursorJson + "}";
        return new Gson().fromJson(json, TransactionPage.class);
    }

    private static final class FakeRepository implements TransactionRepository {
        private final boolean autoDispatch;
        private final Deque<Result> results = new ArrayDeque<>();
        private final List<String> cursors = new ArrayList<>();
        private final List<TransactionFilter> filters = new ArrayList<>();
        private Callback pendingCallback;

        FakeRepository(boolean autoDispatch) {
            this.autoDispatch = autoDispatch;
        }

        void enqueueSuccess(TransactionPage page) {
            results.addLast(Result.success(page));
        }

        void enqueueError(Throwable error) {
            results.addLast(Result.error(error));
        }

        void succeed(TransactionPage page) {
            assertNotNull(pendingCallback);
            Callback callback = pendingCallback;
            pendingCallback = null;
            callback.onSuccess(page);
        }

        @Override
        public void getTransactions(TransactionFilter filter, String cursor, Callback callback) {
            filters.add(filter);
            cursors.add(cursor);
            if (!autoDispatch) {
                pendingCallback = callback;
                return;
            }

            Result result = results.removeFirst();
            if (result.error != null) {
                callback.onError(result.error);
            } else {
                callback.onSuccess(result.page);
            }
        }
    }

    private static final class Result {
        private final TransactionPage page;
        private final Throwable error;

        private Result(TransactionPage page, Throwable error) {
            this.page = page;
            this.error = error;
        }

        static Result success(TransactionPage page) {
            return new Result(page, null);
        }

        static Result error(Throwable error) {
            return new Result(null, error);
        }
    }
}
