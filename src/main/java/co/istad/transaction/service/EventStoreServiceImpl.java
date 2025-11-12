package co.istad.transaction.service;

import co.istad.transaction.aggregate.TransactionAggregate;
import co.istad.transaction.domain.EventStore;
import co.istad.transaction.repository.EventStoreRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventStoreServiceImpl implements EventStoreService {

    private final EventStoreRepository eventStoreRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @Override
    public void saveEvents(TransactionAggregate aggregate, String correlationId) {

        List<Object> events = aggregate.getUncommittedEvents();

        for (Object event : events) {
            try {
                String eventData = objectMapper.writeValueAsString(event);

                EventStore eventStore = new EventStore();
                eventStore.setEventId(UUID.randomUUID());
                eventStore.setAggregateId(aggregate.getTransactionId());
                eventStore.setAggregateType("Transaction");
                eventStore.setVersion(aggregate.getVersion());
                eventStore.setEventType(event.getClass().getSimpleName());
                eventStore.setEventData(eventData);
                eventStore.setTimestamp(Instant.now());

                eventStoreRepository.save(eventStore);

                log.info("Saved event: {} for transaction: {}", eventStore.getEventType(),
                        aggregate.getTransactionId());

            } catch (JsonProcessingException e) {
                log.error("Failed to serialize JSON: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        }

        aggregate.markEventsAsCommitted();
    }

    @Transactional(readOnly = true)
    @Override
    public TransactionAggregate loadAggregate(String transactionId) {

        List<EventStore> eventStores = eventStoreRepository.findByAggregateIdOrderByVersionAsc(transactionId);

        if (eventStores.isEmpty()) {
            return null;
        }

        List<Object> events = new ArrayList<>();
        for (EventStore eventStore : eventStores) {
            // Deserialize event store from database
            try {
                Class<?> clazz = Class.forName("co.istad.transaction.event." + eventStore.getEventType());
                Object event = objectMapper.readValue(eventStore.getEventData().toString(), clazz);
                events.add(event);
            } catch (ClassNotFoundException | JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        TransactionAggregate aggregate = TransactionAggregate.rebuild(transactionId, events);
        log.info("Loaded aggregate: {} for transaction: {}", aggregate.getTransactionId(), transactionId);

        return aggregate;
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventStore> getEventHistory(String transactionId) {
        return eventStoreRepository.findByAggregateIdOrderByVersionAsc(transactionId);
    }

}
