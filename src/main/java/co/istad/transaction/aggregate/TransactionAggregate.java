package co.istad.transaction.aggregate;

import co.istad.transaction.domain.CurrencyEnum;
import co.istad.transaction.domain.TransactionStatus;
import co.istad.transaction.domain.TypeEnum;
import co.istad.transaction.event.TransactionCompensatedEvent;
import co.istad.transaction.event.TransactionCompletedEvent;
import co.istad.transaction.event.TransactionFailedEvent;
import co.istad.transaction.event.TransactionInitiatedEvent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class TransactionAggregate {

    private String transactionId;
    private TypeEnum type;
    private TransactionStatus status;
    private String accountNumber;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String remark;
    private String failureReason;
    private Long version;

    private List<Object> uncommittedEvents = new ArrayList<>();

    public TransactionAggregate(String transactionId) {
        this.transactionId = transactionId;
        this.version = 0L;
    }

    public void initiateDeposit(String accountNumber, BigDecimal amount, CurrencyEnum currency, String remark) {
        validateNewTransaction();
        validateAmount(amount);

        TransactionInitiatedEvent event = TransactionInitiatedEvent.builder()
                .transactionId(transactionId)
                .type(TypeEnum.DEPOSIT)
                .accountNumber(accountNumber)
                .amount(amount)
                .remark(remark)
                .currency(currency)
                .createdAt(Instant.now())
                .build();

        applyEvent(event);
        uncommittedEvents.add(event);
        log.info("Deposit transaction initiated: {}", transactionId);
    }

    private void validateNewTransaction() {
        if (this.version > 0) {
            throw new IllegalStateException("Transaction already initiated");
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    public void markAsCompleted() {
        if (status != TransactionStatus.INITIATED) {
            throw new IllegalStateException("Transaction is not in INITIATED state");
        }

        TransactionCompletedEvent event = TransactionCompletedEvent.builder()
                .transactionId(transactionId)
                .createdAt(Instant.now())
                .build();

        applyEvent(event);
        uncommittedEvents.add(event);
        log.info("Transaction completed: {}", transactionId);
    }

    public void markAsFailed(String reason) {
        if (status != TransactionStatus.INITIATED) {
            throw new IllegalStateException("Transaction is not in INITIATED state");
        }

        TransactionFailedEvent event = TransactionFailedEvent.builder()
                .transactionId(transactionId)
                .reason(reason)
                .createdAt(Instant.now())
                .build();

        applyEvent(event);
        uncommittedEvents.add(event);
        log.error("Transaction failed: {} - {}", transactionId, reason);
    }

    public void markAsCompensated(String reason) {
        TransactionCompensatedEvent event = TransactionCompensatedEvent.builder()
                .transactionId(transactionId)
                .reason(reason)
                .createdAt(Instant.now())
                .build();

        applyEvent(event);
        uncommittedEvents.add(event);
        log.info("Transaction compensated: {}", transactionId);
    }

    public void applyEvent(Object event) {
        if (event instanceof TransactionInitiatedEvent) {
            apply((TransactionInitiatedEvent) event);
        } else if (event instanceof TransactionCompletedEvent) {
            apply((TransactionCompletedEvent) event);
        } else if (event instanceof TransactionFailedEvent) {
            apply((TransactionFailedEvent) event);
        } else if (event instanceof TransactionCompensatedEvent) {
            apply((TransactionCompensatedEvent) event);
        }
        this.version++;
    }

    private void apply(TransactionInitiatedEvent event) {
        this.transactionId = event.getTransactionId();
        this.type = event.getType();
        this.status = TransactionStatus.INITIATED;
        this.accountNumber = event.getAccountNumber();
        this.fromAccountNumber = event.getFromAccountNumber();
        this.toAccountNumber = event.getToAccountNumber();
        this.amount = event.getAmount();
        this.remark = event.getRemark();
    }

    private void apply(TransactionCompletedEvent event) {
        this.status = TransactionStatus.COMPLETED;
    }

    private void apply(TransactionFailedEvent event) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = event.getReason();
    }

    private void apply(TransactionCompensatedEvent event) {
        this.status = TransactionStatus.COMPENSATED;
        this.failureReason = event.getReason();
    }

    public List<Object> getUncommittedEvents() {
        return new ArrayList<>(uncommittedEvents);
    }

    public void markEventsAsCommitted() {
        uncommittedEvents.clear();
    }

    // Rebuild aggregate from event history
    public static TransactionAggregate rebuild(String transactionId, List<Object> events) {
        TransactionAggregate aggregate = new TransactionAggregate(transactionId);
        for (Object event : events) {
            aggregate.applyEvent(event);
        }
        return aggregate;
    }

}
