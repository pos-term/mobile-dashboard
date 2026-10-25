package com.posterm.mobiledashboard.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.posterm.mobiledashboard.R;
import com.posterm.mobiledashboard.model.Transaction;
import com.posterm.mobiledashboard.model.TransactionStatus;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public final class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private final List<Transaction> items = new ArrayList<>();

    public void submitList(List<Transaction> transactions) {
        items.clear();
        items.addAll(transactions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView amount;
        private final TextView terminal;
        private final TextView time;
        private final Chip status;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            amount = itemView.findViewById(R.id.amount);
            terminal = itemView.findViewById(R.id.terminal);
            time = itemView.findViewById(R.id.time);
            status = itemView.findViewById(R.id.status_chip);
        }

        void bind(Transaction transaction) {
            Context context = itemView.getContext();
            amount.setText(formatAmount(transaction.getAmountMinor(), transaction.getCurrency()));
            terminal.setText(context.getString(
                    R.string.transaction_terminal_format,
                    transaction.getTerminalId()
            ));
            time.setText(formatTime(context, transaction.getCreatedAt()));
            bindStatus(context, transaction.getStatus());
        }

        private String formatAmount(long amountMinor, String currencyCode) {
            int fractionDigits = 2;
            String safeCurrencyCode = currencyCode == null ? "" : currencyCode;
            try {
                int currencyDigits = Currency.getInstance(safeCurrencyCode).getDefaultFractionDigits();
                if (currencyDigits >= 0) {
                    fractionDigits = currencyDigits;
                }
            } catch (IllegalArgumentException ignored) {
                // Unknown currencies keep the conventional two decimal places.
            }

            BigDecimal amountValue = BigDecimal.valueOf(amountMinor, fractionDigits);
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
            formatter.setMinimumFractionDigits(fractionDigits);
            formatter.setMaximumFractionDigits(fractionDigits);
            return formatter.format(amountValue) + " " + safeCurrencyCode;
        }

        private String formatTime(Context context, String createdAt) {
            if (createdAt == null) {
                return context.getString(R.string.transaction_time_unknown);
            }

            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US);
            parser.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                Date date = parser.parse(createdAt);
                if (date != null) {
                    return DateFormat.getDateTimeInstance(
                            DateFormat.SHORT,
                            DateFormat.SHORT,
                            Locale.getDefault()
                    ).format(date);
                }
            } catch (ParseException ignored) {
                // Fall back to the server value when its format is unfamiliar.
            }
            return createdAt;
        }

        private void bindStatus(Context context, TransactionStatus transactionStatus) {
            int textResource;
            int backgroundColor;
            int contentColor;

            if (transactionStatus == TransactionStatus.SUCCESS) {
                textResource = R.string.status_success;
                backgroundColor = R.color.status_success_container;
                contentColor = R.color.status_success_content;
            } else if (transactionStatus == TransactionStatus.PENDING) {
                textResource = R.string.status_pending;
                backgroundColor = R.color.status_pending_container;
                contentColor = R.color.status_pending_content;
            } else if (transactionStatus == TransactionStatus.FAILED) {
                textResource = R.string.status_failed;
                backgroundColor = R.color.status_failed_container;
                contentColor = R.color.status_failed_content;
            } else {
                textResource = R.string.status_unknown;
                backgroundColor = R.color.status_unknown_container;
                contentColor = R.color.status_unknown_content;
            }

            status.setText(textResource);
            status.setChipBackgroundColor(ColorStateList.valueOf(
                    ContextCompat.getColor(context, backgroundColor)
            ));
            status.setTextColor(ContextCompat.getColor(context, contentColor));
        }
    }
}
