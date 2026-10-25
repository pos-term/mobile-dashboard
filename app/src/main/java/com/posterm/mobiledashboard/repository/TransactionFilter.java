package com.posterm.mobiledashboard.repository;

import com.posterm.mobiledashboard.model.Transaction;
import com.posterm.mobiledashboard.model.TransactionStatus;

import java.util.Objects;

public final class TransactionFilter {
    private final String terminalId;
    private final TransactionStatus status;
    private final String fromInclusive;
    private final String toExclusive;

    public TransactionFilter(
            String terminalId,
            TransactionStatus status,
            String fromInclusive,
            String toExclusive
    ) {
        this.terminalId = emptyToNull(terminalId);
        this.status = status;
        this.fromInclusive = emptyToNull(fromInclusive);
        this.toExclusive = emptyToNull(toExclusive);
    }

    public static TransactionFilter empty() {
        return new TransactionFilter(null, null, null, null);
    }

    public String getTerminalId() {
        return terminalId;
    }

    public TransactionStatus getStatus() {
        return status;
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
        if (status != null) {
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
        if (status != null && status != transaction.getStatus()) {
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
                && status == other.status
                && Objects.equals(fromInclusive, other.fromInclusive)
                && Objects.equals(toExclusive, other.toExclusive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(terminalId, status, fromInclusive, toExclusive);
    }

    private static String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
