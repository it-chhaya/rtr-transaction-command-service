package co.istad.transaction.domain;

public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    COMPENSATION,
    COMPENSATED
}
