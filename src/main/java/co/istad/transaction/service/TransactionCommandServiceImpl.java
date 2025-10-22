package co.istad.transaction.service;

import co.istad.transaction.domain.EventStore;
import co.istad.transaction.domain.Transaction;
import co.istad.transaction.domain.TypeEnum;
import co.istad.transaction.dto.CreateDepositRequest;
import co.istad.transaction.dto.TransactionResponse;
import co.istad.transaction.repository.EventStoreRepository;
import co.istad.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionCommandServiceImpl
implements TransactionCommandService {

    private final TransactionRepository transactionRepository;
    private final EventStoreRepository eventStoreRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public TransactionResponse createDeposit(CreateDepositRequest createDepositRequest) {

        Transaction transaction = new Transaction();
        transaction.setAccountId(createDepositRequest.accountId());
        transaction.setAmount(createDepositRequest.amount());
        transaction.setCreatedAt(Instant.now());
        transaction.setUpdatedAt(Instant.now());
        transaction.setCreatedBy("admin");
        transaction.setUpdatedBy("admin");
        transaction.setRemark(createDepositRequest.remark());
        transaction.setCurrency(createDepositRequest.currency());
        transaction.setTypeCode(TypeEnum.DEPOSIT);

        // Save data into MongoDB
        transactionRepository.save(transaction);

        // Save event into MongoDB
        EventStore eventStore = new EventStore();
        eventStore.setEventId(UUID.randomUUID());
        eventStore.setEventType("DEPOSIT_CREATED_EVENT");
        eventStore.setAggregateId(transaction.getId());
        eventStore.setAggregateType(Transaction.class.getSimpleName());
        eventStore.setVersion(String.valueOf(eventStoreRepository.countByAggregateId(transaction.getId()) + 1));
        eventStore.setEventData(transaction);

        eventStoreRepository.save(eventStore);

        kafkaTemplate.send("DEPOSIT_CREATED_EVENT", eventStore.getAggregateId(), eventStore);

        return TransactionResponse.builder()
                .transactionId(transaction.getId())
                .amount(transaction.getAmount())
                .build();
    }

}
