package co.istad.transaction.aggregate;

import co.istad.transaction.command.CompleteTransactionCommand;
import co.istad.transaction.command.CreateDepositCommand;
import co.istad.transaction.command.CreateTransferCommand;
import co.istad.transaction.command.FailTransactionCommand;
import co.istad.transaction.domain.CurrencyEnum;
import co.istad.transaction.domain.TransactionStatus;
import co.istad.transaction.domain.TypeEnum;
import co.istad.transaction.event.TransactionCompletedEvent;
import co.istad.transaction.event.TransactionCreatedEvent;
import co.istad.transaction.event.TransactionFailedEvent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
@NoArgsConstructor
public class TransactionAggregate {

    private String id; // transaction ID

    private String fromAccountNumber;
    private String toAccountNumber;

    private TypeEnum typeCode;
    private BigDecimal amount;
    private CurrencyEnum currency;
    private String remark;

    private TransactionStatus status;

    private Long version;
    private List<Object> uncommittedEvents = new ArrayList<>();

    public TransactionAggregate(String id) {
        this.id = id;
        this.version = 0L;
    }


    public void handle(CreateTransferCommand command) {
        // Create event object
        TransactionCreatedEvent event = TransactionCreatedEvent.builder()
                .transactionId(this.id)
                .fromAccountNumber(command.fromAccountNumber())
                .toAccountNumber(command.toAccountNumber())
                .amount(command.amount())
                .currency(command.currency())
                .remark(command.remark())
                .typeCode(TypeEnum.TRANSFER)
                .status(TransactionStatus.PENDING)
                .build();
        this.applyEvent(event);
        this.uncommittedEvents.add(event);
    }


    public void handle(FailTransactionCommand command) {
        // Create event object
        TransactionFailedEvent event = TransactionFailedEvent.builder()
                .transactionId(command.transactionId())
                .status(TransactionStatus.FAILED)
                .remark(command.reason())
                .build();
        this.applyEvent(event);
        this.uncommittedEvents.add(event);
    }

    public void handle(CompleteTransactionCommand command) {
        // Create event object
        TransactionCompletedEvent event = TransactionCompletedEvent.builder()
                .transactionId(command.transactionId())
                .status(TransactionStatus.COMPLETED)
                .build();
        this.applyEvent(event);
        this.uncommittedEvents.add(event);
    }

    public void handle(CreateDepositCommand command) {
        log.info("Handle create deposit command {}", command);
        // Create event object
        TransactionCreatedEvent event = TransactionCreatedEvent.builder()
                .transactionId(this.id)
                .fromAccountNumber(null)
                .toAccountNumber(command.accountNumber())
                .amount(command.amount())
                .currency(command.currency())
                .remark(command.remark())
                .typeCode(TypeEnum.DEPOSIT)
                .status(TransactionStatus.PENDING)
                .build();
        this.applyEvent(event);
        this.uncommittedEvents.add(event);
    }

    private void applyEvent(Object event) {
        if (event instanceof TransactionCreatedEvent transactionCreatedEvent) {
            apply(transactionCreatedEvent);
        } else if (event instanceof TransactionCompletedEvent transactionCompletedEvent) {
            apply(transactionCompletedEvent);
        } else if (event instanceof TransactionFailedEvent transactionFailedEvent) {
            apply(transactionFailedEvent);
        }
    }

    private void apply(TransactionFailedEvent event) {
        this.status = event.getStatus();
        this.remark = event.getRemark();
        this.version++;
    }

    private void apply(TransactionCompletedEvent event) {
        this.status = event.getStatus();
        this.version++;
    }

    private void apply(TransactionCreatedEvent transactionCreatedEvent) {
        this.fromAccountNumber = transactionCreatedEvent.getFromAccountNumber();
        this.toAccountNumber = transactionCreatedEvent.getToAccountNumber();
        this.typeCode = transactionCreatedEvent.getTypeCode();
        this.amount = transactionCreatedEvent.getAmount();
        this.currency = transactionCreatedEvent.getCurrency();
        this.remark = transactionCreatedEvent.getRemark();
        this.status = transactionCreatedEvent.getStatus();

        this.version++;
    }

    public void markEventsAsCommited() {
        this.uncommittedEvents.clear();
    }

    public static TransactionAggregate rebuild(String transactionId, List<Object> events) {
        TransactionAggregate aggregate = new TransactionAggregate(transactionId);
        for (Object event : events) {
            aggregate.applyEvent(event);
        }
        return aggregate;
    }
}
