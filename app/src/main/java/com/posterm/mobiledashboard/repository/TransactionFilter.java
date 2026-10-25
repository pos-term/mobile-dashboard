package com.posterm.mobiledashboard.repository;

import com.posterm.mobiledashboard.model.Transaction;
import com.posterm.mobiledashboard.model.TransactionStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class TransactionFilter {
    private final String terminalId;
    private final List<TransactionStatus> statuses;
    private final String transactionIdPrefix;
    private final Long minAmountMinor;
    private final Long maxAmountMinor;
    private final String currency;
    private final String reason;
    private final String fromInclusive;
    private final String toExclusive;

    public TransactionFilter(
            String terminalId,
            TransactionStatus status,
            String fromInclusive,
            String toExclusive
    ) {
        this(
                terminalId,
                status == null ? Collections.emptyList() : Collections.singletonList(status),
                null,
                null,
                null,
                null,
                null,
                fromInclusive,
                toExclusive
        );
    }

    public TransactionFilter(
            String terminalId,
            List<TransactionStatus> statuses,
            String transactionIdPrefix,
            Long minAmountMinor,
            Long maxAmountMinor,
            String currency,
            String reason,
            String fromInclusive,
            String toExclusive
    ) {
        if (minAmountMinor != null && minAmountMinor < 0) {
            throw new IllegalArgumentException("Minimum amount must not be negative");
        }
        if (maxAmountMinor != null && maxAmountMinor < 0) {
            throw new IllegalArgumentException("Maximum amount must not be negative");
        }
        if (minAmountMinor != null && maxAmountMinor != null && maxAmountMinor < minAmountMinor) {
            throw new IllegalArgumentException("Maximum amount must not be less than minimum amount");
        }

        this.terminalId = emptyToNull(terminalId);
        Set<TransactionStatus> uniqueStatuses = new LinkedHashSet<>();
        if (statuses != null) {
            uniqueStatuses.addAll(statuses);
            uniqueStatuses.remove(null);
        }
        this.statuses = Collections.unmodifiableList(new ArrayList<>(uniqueStatuses));
        this.transactionIdPrefix = emptyToNull(transactionIdPrefix);
        this.minAmountMinor = minAmountMinor;
        this.maxAmountMinor = maxAmountMinor;
        this.currency = normalizeCurrency(currency);
        this.reason = emptyToNull(reason);
        this.fromInclusive = emptyToNull(fromInclusive);
        this.toExclusive = emptyToNull(toExclusive);
    }

    public static TransactionFilter empty() {
        return new TransactionFilter(null, null, null, null);
    }

    public String getTerminalId() {
        return terminalId;
    }

    public List<TransactionStatus> getStatuses() {
        return statuses;
    }

    public String getTransactionIdPrefix() {
        return transactionIdPrefix;
    }

    public Long getMinAmountMinor() {
        return minAmountMinor;
    }

    public Long getMaxAmountMinor() {
        return maxAmountMinor;
    }

    public String getCurrency() {
        return currency;
    }

    public String getReason() {
        return reason;
    }

    public String getFromInclusive() {
        return fromInclusive;
    }

    public String getToExclusive() {
        return toExclusive;
    }

    public int activeFilterCount() {
        int count = 0;
        if (terminalId != null) {
            count++;
        }
        if (!statuses.isEmpty()) {
            count++;
        }
        if (transactionIdPrefix != null) {
            count++;
        }
        if (minAmountMinor != null || maxAmountMinor != null) {
            count++;
        }
        if (currency != null) {
            count++;
        }
        if (reason != null) {
            count++;
        }
        if (fromInclusive != null || toExclusive != null) {
            count++;
        }
        return count;
    }

    public boolean matches(Transaction transaction) {
        if (terminalId != null && !terminalId.equalsIgnoreCase(transaction.getTerminalId())) {
            return false;
        }
        if (!statuses.isEmpty() && !statuses.contains(transaction.getStatus())) {
            return false;
        }
        if (transactionIdPrefix != null
                && !startsWithIgnoreCase(transaction.getTransactionId(), transactionIdPrefix)) {
            return false;
        }
        if (minAmountMinor != null && transaction.getAmountMinor() < minAmountMinor) {
            return false;
        }
        if (maxAmountMinor != null && transaction.getAmountMinor() > maxAmountMinor) {
            return false;
        }
        if (currency != null && !currency.equalsIgnoreCase(transaction.getCurrency())) {
            return false;
        }
        if (reason != null && !reason.equalsIgnoreCase(transaction.getReason())) {
            return false;
        }

        String createdAt = transaction.getCreatedAt();
        if (createdAt == null) {
            return fromInclusive == null && toExclusive == null;
        }
        if (fromInclusive != null && createdAt.compareTo(fromInclusive) < 0) {
            return false;
        }
        return toExclusive == null || createdAt.compareTo(toExclusive) < 0;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof TransactionFilter)) {
            return false;
        }
        TransactionFilter other = (TransactionFilter) object;
        return Objects.equals(terminalId, other.terminalId)
                && Objects.equals(statuses, other.statuses)
                && Objects.equals(transactionIdPrefix, other.transactionIdPrefix)
                && Objects.equals(minAmountMinor, other.minAmountMinor)
                && Objects.equals(maxAmountMinor, other.maxAmountMinor)
                && Objects.equals(currency, other.currency)
                && Objects.equals(reason, other.reason)
                && Objects.equals(fromInclusive, other.fromInclusive)
                && Objects.equals(toExclusive, other.toExclusive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                terminalId,
                statuses,
                transactionIdPrefix,
                minAmountMinor,
                maxAmountMinor,
                currency,
                reason,
                fromInclusive,
                toExclusive
        );
    }

    private static String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private static String normalizeCurrency(String value) {
        String normalized = emptyToNull(value);
        if (normalized == null) {
            return null;
        }
        normalized = normalized.toUpperCase(Locale.US);
        if (!normalized.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Currency must be a three-letter ISO 4217 code");
        }
        return normalized;
    }

    private static boolean startsWithIgnoreCase(String value, String prefix) {
        return value != null
                && value.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
