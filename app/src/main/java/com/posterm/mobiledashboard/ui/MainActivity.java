package com.posterm.mobiledashboard.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.posterm.mobiledashboard.R;
import com.posterm.mobiledashboard.model.TransactionStatus;
import com.posterm.mobiledashboard.repository.TransactionFilter;
import com.posterm.mobiledashboard.repository.TransactionRepository;
import com.posterm.mobiledashboard.repository.TransactionRepositoryProvider;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public final class MainActivity extends AppCompatActivity {
    private static final long DAY_MILLIS = 24L * 60L * 60L * 1000L;
    private static final String API_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private TransactionAdapter adapter;
    private MaterialButton filterButton;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView transactionList;
    private ProgressBar loadingIndicator;
    private ProgressBar paginationIndicator;
    private View emptyState;
    private View errorState;
    private TextView emptyMessage;
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

        filterButton.setOnClickListener(view -> showFilters(viewModel));
        swipeRefresh.setOnRefreshListener(viewModel::refresh);
        MaterialButton retryButton = findViewById(R.id.retry_button);
        retryButton.setOnClickListener(view -> viewModel.retry());
        viewModel.getState().observe(this, state -> render(state, viewModel));
    }

    private void bindViews() {
        swipeRefresh = findViewById(R.id.swipe_refresh);
        filterButton = findViewById(R.id.filter_button);
        transactionList = findViewById(R.id.transaction_list);
        loadingIndicator = findViewById(R.id.loading_indicator);
        paginationIndicator = findViewById(R.id.pagination_indicator);
        emptyState = findViewById(R.id.empty_state);
        emptyMessage = findViewById(R.id.empty_message);
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
        TransactionFilter filter = viewModel.getCurrentFilter();
        int activeFilters = filter.activeFilterCount();
        filterButton.setText(activeFilters == 0
                ? getString(R.string.filters)
                : getString(R.string.filters_count, activeFilters));
        emptyMessage.setText(activeFilters == 0
                ? R.string.transactions_empty_message
                : R.string.transactions_filtered_empty_message);

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

    private void showFilters(TransactionListViewModel viewModel) {
        TransactionFilter currentFilter = viewModel.getCurrentFilter();
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_transaction_filters, null);
        TextInputEditText transactionIdField = dialogView.findViewById(
                R.id.filter_transaction_id
        );
        TextInputEditText terminalField = dialogView.findViewById(R.id.filter_terminal);
        TextInputEditText minAmountField = dialogView.findViewById(R.id.filter_min_amount);
        TextInputEditText maxAmountField = dialogView.findViewById(R.id.filter_max_amount);
        TextInputEditText currencyField = dialogView.findViewById(R.id.filter_currency);
        TextInputEditText reasonField = dialogView.findViewById(R.id.filter_reason);
        Chip pendingChip = dialogView.findViewById(R.id.filter_status_pending);
        Chip successChip = dialogView.findViewById(R.id.filter_status_success);
        Chip failedChip = dialogView.findViewById(R.id.filter_status_failed);
        MaterialButton dateRangeButton = dialogView.findViewById(R.id.filter_date_range);

        transactionIdField.setText(currentFilter.getTransactionIdPrefix());
        terminalField.setText(currentFilter.getTerminalId());
        setAmountValue(
                minAmountField,
                currentFilter.getMinAmountMinor(),
                currentFilter.getCurrency()
        );
        setAmountValue(
                maxAmountField,
                currentFilter.getMaxAmountMinor(),
                currentFilter.getCurrency()
        );
        currencyField.setText(currentFilter.getCurrency());
        reasonField.setText(currentFilter.getReason());
        pendingChip.setChecked(currentFilter.getStatuses().contains(TransactionStatus.PENDING));
        successChip.setChecked(currentFilter.getStatuses().contains(TransactionStatus.SUCCESS));
        failedChip.setChecked(currentFilter.getStatuses().contains(TransactionStatus.FAILED));

        long fromMillis = parseApiDate(currentFilter.getFromInclusive());
        long toExclusiveMillis = parseApiDate(currentFilter.getToExclusive());
        long[] selectedRange = {
                fromMillis,
                toExclusiveMillis < 0 ? -1L : toExclusiveMillis - DAY_MILLIS
        };
        updateDateRangeButton(dateRangeButton, selectedRange);
        dateRangeButton.setOnClickListener(view -> {
            MaterialDatePicker.Builder<Pair<Long, Long>> builder =
                    MaterialDatePicker.Builder.dateRangePicker()
                            .setTitleText(R.string.filter_date_range);
            if (selectedRange[0] >= 0 && selectedRange[1] >= 0) {
                builder.setSelection(Pair.create(selectedRange[0], selectedRange[1]));
            }
            MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
            picker.addOnPositiveButtonClickListener(selection -> {
                if (selection != null && selection.first != null && selection.second != null) {
                    selectedRange[0] = selection.first;
                    selectedRange[1] = selection.second;
                    updateDateRangeButton(dateRangeButton, selectedRange);
                }
            });
            picker.show(getSupportFragmentManager(), "transaction-date-range");
        });

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.filter_dialog_title)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.filter_reset, (dialogInterface, which) ->
                        viewModel.applyFilter(TransactionFilter.empty()))
                .setPositiveButton(R.string.filter_apply, null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> {
                    try {
                        List<TransactionStatus> statuses = new ArrayList<>();
                        if (pendingChip.isChecked()) {
                            statuses.add(TransactionStatus.PENDING);
                        }
                        if (successChip.isChecked()) {
                            statuses.add(TransactionStatus.SUCCESS);
                        }
                        if (failedChip.isChecked()) {
                            statuses.add(TransactionStatus.FAILED);
                        }

                        String from = selectedRange[0] < 0
                                ? null
                                : formatApiDate(selectedRange[0]);
                        String to = selectedRange[1] < 0
                                ? null
                                : formatApiDate(selectedRange[1] + DAY_MILLIS);
                        String currency = textOf(currencyField);
                        TransactionFilter filter = new TransactionFilter(
                                textOf(terminalField),
                                statuses,
                                textOf(transactionIdField),
                                amountMinorValueOf(minAmountField, currency),
                                amountMinorValueOf(maxAmountField, currency),
                                currency,
                                textOf(reasonField),
                                from,
                                to
                        );
                        viewModel.applyFilter(filter);
                        dialog.dismiss();
                    } catch (IllegalArgumentException error) {
                        Snackbar.make(
                                dialogView,
                                R.string.filter_invalid_values,
                                Snackbar.LENGTH_LONG
                        ).show();
                    }
                }));
        dialog.show();
    }

    private String textOf(TextInputEditText field) {
        return field.getText() == null ? null : field.getText().toString();
    }

    private Long amountMinorValueOf(TextInputEditText field, String currencyCode) {
        String value = textOf(field);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return CurrencyAmountConverter.toMinorUnits(value, currencyCode);
    }

    private void setAmountValue(TextInputEditText field, Long value, String currencyCode) {
        if (value == null) {
            field.setText(null);
        } else {
            field.setText(CurrencyAmountConverter.toMajorUnits(value, currencyCode));
        }
    }

    private void updateDateRangeButton(MaterialButton button, long[] selectedRange) {
        if (selectedRange[0] < 0 || selectedRange[1] < 0) {
            button.setText(R.string.filter_date_range);
            return;
        }
        button.setText(getString(
                R.string.filter_date_range_value,
                formatDisplayDate(selectedRange[0]),
                formatDisplayDate(selectedRange[1])
        ));
    }

    private String formatDisplayDate(long millis) {
        return DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
                .format(new Date(millis));
    }

    private String formatApiDate(long millis) {
        SimpleDateFormat formatter = new SimpleDateFormat(API_DATE_PATTERN, Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(new Date(millis));
    }

    private long parseApiDate(String value) {
        if (value == null) {
            return -1L;
        }
        SimpleDateFormat parser = new SimpleDateFormat(API_DATE_PATTERN, Locale.US);
        parser.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date parsed = parser.parse(value);
            return parsed == null ? -1L : parsed.getTime();
        } catch (ParseException ignored) {
            return -1L;
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
