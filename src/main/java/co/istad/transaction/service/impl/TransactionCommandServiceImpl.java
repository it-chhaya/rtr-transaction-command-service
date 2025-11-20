package co.istad.transaction.service.impl;

import co.istad.transaction.aggregate.TransactionAggregate;
import co.istad.transaction.command.CompleteTransactionCommand;
import co.istad.transaction.command.CreateDepositCommand;
import co.istad.transaction.command.FailTransactionCommand;
import co.istad.transaction.event.TransactionCompletedEvent;
import co.istad.transaction.publisher.EventPublisher;
import co.istad.transaction.service.EventStoreService;
import co.istad.transaction.service.TransactionCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionCommandServiceImpl
    implements TransactionCommandService {

    private final EventStoreService eventStoreService;
    private final EventPublisher eventPublisher;

    @Override
    public String createDeposit(CreateDepositCommand createDepositCommand) {
        log.info("create deposit command: {}", createDepositCommand);

        // Create aggregate
        String transactionId = UUID.randomUUID().toString();
        TransactionAggregate aggregate =
                new TransactionAggregate(transactionId);
        // Handle aggregate logic
        aggregate.handle(createDepositCommand);

        // Persist event sourcing
        eventStoreService.saveEvents(aggregate, transactionId);

        // Update read model (Event Publisher)
        eventPublisher.publishEvents("banking.transaction.deposited", aggregate.getUncommittedEvents());

        // Commit event
        aggregate.markEventsAsCommited();

        return transactionId;
    }

    @Override
    public void handleCompleteTransactionCommand(CompleteTransactionCommand command) {
        log.info("handle deposited event: {}", command);

        // Load aggregate object
        TransactionAggregate aggregate = eventStoreService
                .loadAggregate(command.transactionId());

        if (aggregate == null) {
            throw new RuntimeException("Transaction not found: " + command.transactionId());
        }

        // Handle aggregate logic
        aggregate.handle(command);

        // Persist event sourcing
        eventStoreService.saveEvents(aggregate, command.transactionId());

        // Update read model
        eventPublisher.publishEvents("banking.transaction.completed", aggregate.getUncommittedEvents());

        aggregate.markEventsAsCommited();
    }

    @Override
    public void handleFailTransactionCommand(FailTransactionCommand command) {
        log.info("handle fail transaction event: {}", command);

        // Load aggregate object
        TransactionAggregate aggregate = eventStoreService
                .loadAggregate(command.transactionId());

        if (aggregate == null) {
            throw new RuntimeException("Transaction not found: " + command.transactionId());
        }

        // Handle aggregate logic
        aggregate.handle(command);

        // Persist event sourcing
        eventStoreService.saveEvents(aggregate, command.transactionId());

        // Update read model
        eventPublisher.publishEvents("banking.transaction.failed", aggregate.getUncommittedEvents());

        aggregate.markEventsAsCommited();
    }

}
