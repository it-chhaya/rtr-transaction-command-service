package co.istad.transaction.service;

import co.istad.transaction.event.TransactionCompletedEvent;
import co.istad.transaction.event.TransactionFailedEvent;
import co.istad.transaction.event.TransactionInitiatedEvent;

public interface TransactionCommandService {

    void initializeTransaction(TransactionInitiatedEvent event, Long version);

    void handleTransactionCompleted(TransactionCompletedEvent event, Long version);

    void handleTransactionFailed(TransactionFailedEvent event, Long version);
}
