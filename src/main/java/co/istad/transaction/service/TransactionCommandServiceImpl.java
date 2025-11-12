package co.istad.transaction.service;

import co.istad.transaction.domain.EventStore;
import co.istad.transaction.domain.Transaction;
import co.istad.transaction.domain.TransactionStatus;
import co.istad.transaction.domain.TypeEnum;
import co.istad.transaction.dto.CreateDepositRequest;
import co.istad.transaction.dto.TransactionResponse;
import co.istad.transaction.event.TransactionDepositedEvent;
import co.istad.transaction.repository.EventStoreRepository;
import co.istad.transaction.repository.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionCommandServiceImpl
implements TransactionCommandService {

    private final TransactionRepository transactionRepository;
    private final EventStoreRepository eventStoreRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public TransactionResponse createDeposit(CreateDepositRequest createDepositRequest) {

        Transaction transaction = new Transaction();
        transaction.setAccountNumber(createDepositRequest.accountNumber());
        transaction.setAmount(createDepositRequest.amount());
        transaction.setCreatedAt(Instant.now());
        transaction.setUpdatedAt(Instant.now());
        transaction.setCreatedBy("admin");
        transaction.setUpdatedBy("admin");
        transaction.setRemark(createDepositRequest.remark());
        transaction.setCurrency(createDepositRequest.currency());
        transaction.setTypeCode(TypeEnum.DEPOSIT);
        transaction.setStatus(TransactionStatus.PENDING);

        // Save data into MongoDB
        transactionRepository.save(transaction);

        TransactionDepositedEvent eventData = TransactionDepositedEvent.builder()
                .accountNumber(transaction.getAccountNumber())
                .amount(transaction.getAmount())
                .txnId(transaction.getId())
                .build();

        // Save event into MongoDB
        EventStore eventStore = new EventStore();
        eventStore.setEventId(UUID.randomUUID());
        eventStore.setEventType("TransactionDeposited");
        eventStore.setAggregateId(transaction.getId());
        eventStore.setAggregateType(Transaction.class.getSimpleName());
        eventStore.setVersion(String.valueOf(eventStoreRepository.countByAggregateId(transaction.getId()) + 1));
        try {
            eventStore.setEventData(objectMapper.writeValueAsString(eventData));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        eventStoreRepository.save(eventStore);

        kafkaTemplate.send("banking.transaction.deposited", eventStore.getAggregateId(), eventData);

        return TransactionResponse.builder()
                .transactionId(transaction.getId())
                .amount(transaction.getAmount())
                .build();
    }

}
