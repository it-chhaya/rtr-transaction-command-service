package co.istad.transaction.domain;

public enum TransactionStatus {
    INITIATED,
    PENDING,
    COMPLETED,
    FAILED,
    COMPENSATION,
    COMPENSATED
}
