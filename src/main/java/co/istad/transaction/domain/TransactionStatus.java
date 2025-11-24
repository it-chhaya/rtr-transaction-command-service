package co.istad.transaction.domain;

public enum TransactionStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    COMPENSATION,
    COMPENSATED
}
