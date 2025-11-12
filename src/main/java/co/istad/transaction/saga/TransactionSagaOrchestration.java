package co.istad.transaction.saga;

import co.istad.transaction.aggregate.TransactionAggregate;
import co.istad.transaction.domain.CurrencyEnum;
import co.istad.transaction.event.DepositRequestedEvent;
import co.istad.transaction.event.TransactionCompletedEvent;
import co.istad.transaction.event.TransactionFailedEvent;
import co.istad.transaction.event.TransactionInitiatedEvent;
import co.istad.transaction.service.EventStoreService;
import co.istad.transaction.service.TransactionCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionSagaOrchestration {

    private final EventStoreService eventStoreService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TransactionCommandService transactionCommandService;

    @Transactional
    public String initiateDeposit(String accountNumber, BigDecimal amount, CurrencyEnum currency, String remark) {

        // Generate new transaction ID
        String transactionId = UUID.randomUUID().toString();

        // Create new aggregate
        TransactionAggregate aggregate = new TransactionAggregate(transactionId);
        aggregate.initiateDeposit(accountNumber, amount, currency,remark);

        // Backup events
        List<Object> events = aggregate.getUncommittedEvents();

        // Persist event sourcing
        eventStoreService.saveEvents(aggregate, transactionId);

        // Update service
        for (Object event : events) {
            transactionCommandService.initializeTransaction((TransactionInitiatedEvent) event, aggregate.getVersion());
        }

        log.info("Deposit transaction initiated: {}", transactionId);

        // Publish event to account service
        DepositRequestedEvent depositRequestedEvent = DepositRequestedEvent.builder()
                .transactionId(transactionId)
                .accountNumber(accountNumber)
                .amount(amount)
                .currency(currency)
                .remark(remark)
                .build();

        kafkaTemplate.send("deposit-requested", depositRequestedEvent);
        log.info("Deposit requested event published: {}", depositRequestedEvent);

        return transactionId;
    }


    @Transactional
    public void handleDepositCompleted(String transactionId) {

        // Load aggregate from event sourcing
        TransactionAggregate aggregate = eventStoreService.loadAggregate(transactionId);

        if (aggregate == null) {
            log.error("Transaction not found: {}", transactionId);
            return;
        }

        // Make that event completed
        aggregate.markAsCompleted();

        // Back up event objects
        List<Object> events = aggregate.getUncommittedEvents();

        // Persist event sourcing
        eventStoreService.saveEvents(aggregate, transactionId);

        // Update projection
        for (Object event : events) {
            transactionCommandService.handleTransactionCompleted((TransactionCompletedEvent) event, aggregate.getVersion());
        }

        log.info("Deposit transaction completed: {}", transactionId);
    }

    public void handleDepositFailed(String transactionId, String reason) {

        // Load aggregate from event sourcing
        TransactionAggregate aggregate = eventStoreService.loadAggregate(transactionId);

        // Validate transaction
        if (aggregate == null) {
            log.error("Transaction not found {}", transactionId);
            return;
        }

        // Mark transaction as failed
        aggregate.markAsFailed(reason);

        // Backup event objects
        List<Object> events = aggregate.getUncommittedEvents();

        eventStoreService.saveEvents(aggregate, transactionId);

        for (Object event : events) {
            transactionCommandService.handleTransactionFailed((TransactionFailedEvent) event, aggregate.getVersion());
        }

        log.error("Deposit transaction failed: {} - {}", transactionId, reason);
    }
}
