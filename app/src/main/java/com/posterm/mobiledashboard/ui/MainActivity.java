package com.posterm.mobiledashboard.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.posterm.mobiledashboard.R;
import com.posterm.mobiledashboard.repository.TransactionRepository;
import com.posterm.mobiledashboard.repository.TransactionRepositoryProvider;

public final class MainActivity extends AppCompatActivity {
    private TransactionAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView transactionList;
    private ProgressBar loadingIndicator;
    private ProgressBar paginationIndicator;
    private View emptyState;
    private View errorState;
    private String lastShownError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        applySystemBarInsets();

        bindViews();
        TransactionListViewModel viewModel = createViewModel();
        configureList(viewModel);

        swipeRefresh.setOnRefreshListener(viewModel::refresh);
        MaterialButton retryButton = findViewById(R.id.retry_button);
        retryButton.setOnClickListener(view -> viewModel.retry());
        viewModel.getState().observe(this, state -> render(state, viewModel));
    }

    private void bindViews() {
        swipeRefresh = findViewById(R.id.swipe_refresh);
        transactionList = findViewById(R.id.transaction_list);
        loadingIndicator = findViewById(R.id.loading_indicator);
        paginationIndicator = findViewById(R.id.pagination_indicator);
        emptyState = findViewById(R.id.empty_state);
        errorState = findViewById(R.id.error_state);
        adapter = new TransactionAdapter();
    }

    private TransactionListViewModel createViewModel() {
        TransactionRepository repository = TransactionRepositoryProvider.create(this);
        ViewModelProvider.Factory factory = new ViewModelProvider.Factory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (!modelClass.isAssignableFrom(TransactionListViewModel.class)) {
                    throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
                }
                return (T) new TransactionListViewModel(repository);
            }
        };
        return new ViewModelProvider(this, factory).get(TransactionListViewModel.class);
    }

    private void configureList(TransactionListViewModel viewModel) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        transactionList.setLayoutManager(layoutManager);
        transactionList.setAdapter(adapter);
        transactionList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) {
                    return;
                }
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                if (lastVisible >= adapter.getItemCount() - 3) {
                    viewModel.loadNextPage();
                }
            }
        });
    }

    private void render(TransactionListState state, TransactionListViewModel viewModel) {
        adapter.submitList(state.getTransactions());
        swipeRefresh.setRefreshing(state.isRefreshing());
        paginationIndicator.setVisibility(state.isLoadingMore() ? View.VISIBLE : View.GONE);

        loadingIndicator.setVisibility(
                state.getStatus() == TransactionListState.Status.LOADING ? View.VISIBLE : View.GONE
        );
        transactionList.setVisibility(
                state.getStatus() == TransactionListState.Status.CONTENT ? View.VISIBLE : View.GONE
        );
        emptyState.setVisibility(
                state.getStatus() == TransactionListState.Status.EMPTY ? View.VISIBLE : View.GONE
        );
        errorState.setVisibility(
                state.getStatus() == TransactionListState.Status.ERROR ? View.VISIBLE : View.GONE
        );
        swipeRefresh.setEnabled(
                state.getStatus() == TransactionListState.Status.CONTENT && !state.isLoadingMore()
        );

        String errorMessage = state.getErrorMessage();
        if (state.getStatus() == TransactionListState.Status.CONTENT
                && errorMessage != null
                && !errorMessage.equals(lastShownError)) {
            lastShownError = errorMessage;
            Snackbar.make(swipeRefresh, R.string.transactions_error_message, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry, view -> viewModel.retry())
                    .show();
        } else if (errorMessage == null) {
            lastShownError = null;
        }
    }

    private void applySystemBarInsets() {
        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
