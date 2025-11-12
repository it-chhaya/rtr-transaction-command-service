package co.istad.transaction.service;

import co.istad.transaction.domain.Transaction;
import co.istad.transaction.domain.TransactionStatus;
import co.istad.transaction.domain.TypeEnum;
import co.istad.transaction.event.TransactionCompletedEvent;
import co.istad.transaction.event.TransactionFailedEvent;
import co.istad.transaction.event.TransactionInitiatedEvent;
import co.istad.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionCommandServiceImpl implements TransactionCommandService {

    private final TransactionRepository transactionRepository;

    @Override
    public void initializeTransaction(TransactionInitiatedEvent event, Long version) {

        Transaction transaction = new Transaction();
        transaction.setTransactionId(event.getTransactionId());
        transaction.setAccountNumber(event.getAccountNumber());
        transaction.setAmount(event.getAmount());
        transaction.setToAccountNumber(event.getToAccountNumber());
        transaction.setFromAccountNumber(event.getFromAccountNumber());
        transaction.setCreatedAt(Instant.now());
        transaction.setUpdatedAt(Instant.now());
        transaction.setCreatedBy("admin");
        transaction.setUpdatedBy("admin");
        transaction.setRemark(event.getRemark());
        transaction.setCurrency(event.getCurrency());
        transaction.setTypeCode(TypeEnum.DEPOSIT);
        transaction.setStatus(TransactionStatus.INITIATED);
        transaction.setVersion(version);

        // Save data into MongoDB
        transactionRepository.save(transaction);

        log.info("transaction saved: {}", event.getTransactionId());
    }


    @Override
    public void handleTransactionCompleted(TransactionCompletedEvent event, Long version) {
        Transaction transaction = transactionRepository.findByTransactionId(event.getTransactionId())
                .orElseThrow(() -> new RuntimeException("transaction not found: " + event.getTransactionId()));
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setVersion(version);
        transaction.setUpdatedAt(Instant.now());
        transaction.setUpdatedBy("rtr");

        transactionRepository.save(transaction);
        log.info("Transaction updated as completed: {}", event.getTransactionId());
    }

    @Override
    public void handleTransactionFailed(TransactionFailedEvent event, Long version) {
        Transaction transaction = transactionRepository.findByTransactionId(event.getTransactionId())
                .orElseThrow(() -> new RuntimeException("transaction not found: " + event.getTransactionId()));
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setVersion(version);
        transaction.setUpdatedAt(Instant.now());
        transaction.setUpdatedBy("rtr");
        transactionRepository.save(transaction);

        log.info("Transaction failed as completed: {}", event.getTransactionId());
    }
}
