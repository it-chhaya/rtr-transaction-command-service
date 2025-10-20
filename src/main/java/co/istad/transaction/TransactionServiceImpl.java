package co.istad.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService{

    private final EventStoreRepository eventStoreRepository;

    @Override
    public void deposit() {
        EventStore eventStore = new EventStore();
        eventStore.setEventId(UUID.randomUUID());
        eventStore.setEventType("depositedEvent");
        eventStore.setVersion("v1");
        eventStore.setAggregateType("Account");
        eventStore.setAggregateId(UUID.randomUUID().toString());

        DepositedEvent depositedEvent = new DepositedEvent();
        depositedEvent.setAccountId("88889999");
        depositedEvent.setTransactionId("TXN88889999");
        depositedEvent.setAmount(BigDecimal.valueOf(5000));
        eventStore.setEventData(depositedEvent);

        eventStoreRepository.save(eventStore);
    }

}
