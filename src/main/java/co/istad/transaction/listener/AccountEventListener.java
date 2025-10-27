package co.istad.transaction.listener;

import co.istad.transaction.domain.EventStore;
import co.istad.transaction.domain.Transaction;
import co.istad.transaction.domain.TransactionStatus;
import co.istad.transaction.repository.EventStoreRepository;
import co.istad.transaction.repository.TransactionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventListener {

    private final TransactionRepository transactionRepository;
    private final EventStoreRepository eventStoreRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "account-events", groupId = "${spring.application.name}")
    public void handleAccountEvents(EventStore eventStore) {
        log.info("Received event: {}", eventStore);

        String eventType = eventStore.getEventType();

        switch (eventType) {
            case "AccountCredited" -> handleAccountCredited(eventStore);
            case "AccountCreditFailed" -> handleAccountCreditFailed(eventStore);
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        }

    }

    private void handleAccountCreditFailed(EventStore eventStore) {
    }

    private void handleAccountCredited(EventStore eventData) {
        // Validate transaction
        String txnId = eventData.getEventData().get("txnId").toString();

        Transaction transaction = transactionRepository
                .findById(txnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid transaction"));
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setUpdatedAt(Instant.now());
        transaction.setUpdatedBy("rtr");
        transactionRepository.save(transaction);

        EventStore eventStore = new EventStore();
        eventStore.setEventId(UUID.randomUUID());
        eventStore.setEventType("TransactionDepositCompleted");
        eventStore.setAggregateId(transaction.getId());
        eventStore.setAggregateType(Transaction.class.getSimpleName());
        eventStore.setVersion(String.valueOf(eventStoreRepository.countByAggregateId(transaction.getId()) + 1));
        eventStore.setEventData(objectMapper.convertValue(transaction,
                new TypeReference<Map<String, Object>>() {
                }));

        eventStoreRepository.save(eventStore);
        kafkaTemplate.send("transaction-events", eventStore.getAggregateId(), eventStore);
    }
}
