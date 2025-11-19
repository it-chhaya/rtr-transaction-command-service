package co.istad.transaction.service;

import co.istad.transaction.domain.Transaction;
import co.istad.transaction.event.TransactionCompletedEvent;
import co.istad.transaction.event.TransactionCreatedEvent;
import co.istad.transaction.event.TransactionFailedEvent;
import co.istad.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionProjectionService {

    private final TransactionRepository transactionRepository;

    @Transactional
    public void onProjection(Object event) {
        log.info("Projection event {}", event);
        if (event instanceof TransactionCreatedEvent transactionCreatedEvent) {
            handle(transactionCreatedEvent);
        } else if (event instanceof TransactionCompletedEvent transactionCompletedEvent) {
            handle(transactionCompletedEvent);
        } else if (event instanceof TransactionFailedEvent transactionFailedEvent) {
            handle(transactionFailedEvent);
        }
    }

    private void handle(TransactionCreatedEvent transactionCreatedEvent) {
        Transaction transaction = new Transaction();
        transaction.setId(transactionCreatedEvent.getTransactionId());
        transaction.setFromAccountNumber(transactionCreatedEvent.getFromAccountNumber());
        transaction.setToAccountNumber(transactionCreatedEvent.getToAccountNumber());
        transaction.setAmount(transactionCreatedEvent.getAmount());
        transaction.setCreatedAt(Instant.now());
        transaction.setUpdatedAt(Instant.now());
        transaction.setCreatedBy("admin");
        transaction.setUpdatedBy("admin");
        transaction.setRemark(transactionCreatedEvent.getRemark());
        transaction.setCurrency(transactionCreatedEvent.getCurrency());
        transaction.setTypeCode(transactionCreatedEvent.getTypeCode());
        transaction.setStatus(transactionCreatedEvent.getStatus());
        transaction.setVersion(1L);

        // Save data into MongoDB
        transactionRepository.save(transaction);
        log.info("Transaction document saved {}", transaction.getId());
    }

    private void handle(TransactionCompletedEvent transactionCompletedEvent) {
        Transaction transaction = transactionRepository.findById(transactionCompletedEvent.getTransactionId())
                .orElseThrow(() -> new RuntimeException("transaction not found: " + transactionCompletedEvent.getTransactionId()));
        transaction.setVersion(transaction.getVersion() + 1);
        transaction.setStatus(transactionCompletedEvent.getStatus());
        transaction.setUpdatedBy("admin");
        transaction.setUpdatedAt(Instant.now());
        transactionRepository.save(transaction);
        log.info("Transaction document completed {}", transaction.getId());
    }

    private void handle(TransactionFailedEvent transactionFailedEvent) {
        Transaction transaction = transactionRepository.findById(transactionFailedEvent.getTransactionId())
                .orElseThrow(() -> new RuntimeException("transaction not found: " + transactionFailedEvent.getTransactionId()));
        transaction.setVersion(transaction.getVersion() + 1);
        transaction.setStatus(transactionFailedEvent.getStatus());
        transaction.setRemark(transactionFailedEvent.getRemark());
        transaction.setUpdatedBy("admin");
        transaction.setUpdatedAt(Instant.now());
        transactionRepository.save(transaction);
        log.info("Transaction document failed {}", transaction.getId());
    }

}
