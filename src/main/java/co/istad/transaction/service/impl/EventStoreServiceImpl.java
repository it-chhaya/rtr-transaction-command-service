package co.istad.transaction.service.impl;

import co.istad.transaction.aggregate.TransactionAggregate;
import co.istad.transaction.domain.EventStore;
import co.istad.transaction.domain.Transaction;
import co.istad.transaction.repository.EventStoreRepository;
import co.istad.transaction.service.EventStoreService;
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

    private final ObjectMapper objectMapper;
    private final EventStoreRepository eventStoreRepository;

    @Transactional
    @Override
    public void saveEvents(TransactionAggregate aggregate, String correlationId) {

        List<Object> events = aggregate.getUncommittedEvents();
        log.info("save events size {}", events.size());

        // Persist event sourcing
        for (Object event : events) {
            try {
                String eventData = objectMapper.writeValueAsString(event);

                EventStore eventStore = new EventStore();
                eventStore.setId(UUID.randomUUID().toString());
                eventStore.setEventId(UUID.randomUUID());
                eventStore.setEventType(event.getClass().getSimpleName());
                eventStore.setAggregateId(correlationId);
                eventStore.setAggregateType(Transaction.class.getSimpleName());
                eventStore.setTimestamp(Instant.now());
                eventStore.setEventData(eventData);
                eventStore.setVersion(aggregate.getVersion());

                eventStoreRepository.save(eventStore);

                log.info("Event stored: {} for aggregate {}", event.getClass().getSimpleName(), aggregate.getId());
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize event: {}", e.getMessage());
                throw new RuntimeException("Event serialization failed", e);
            }
        }

        //aggregate.markEventsAsCommitted();
    }

    @Transactional(readOnly = true)
    @Override
    public TransactionAggregate loadAggregate(String transactionId) {

        List<EventStore> eventStores = eventStoreRepository
                .findByAggregateIdOrderByVersionAsc(transactionId);

        if (eventStores.isEmpty()) {
            return null;
        }

        // Create array list empty
        List<Object> events = new ArrayList<>();

        for (EventStore eventStore : eventStores) {
            try {
                Class<?> eventClass = Class.forName(
                        "co.istad.transaction.event." + eventStore.getEventType());
                Object event = objectMapper.readValue(eventStore.getEventData(), eventClass);
                events.add(event);
            } catch (Exception e) {
                log.error("Failed to deserialize event: {}", e.getMessage());
                throw new RuntimeException("Event deserialization failed", e);
            }
        }

        TransactionAggregate aggregate = TransactionAggregate.rebuild(transactionId, events);
        log.info("Aggregate rebuilt from {} events for account {}", events.size(), transactionId);

        return aggregate;
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventStore> getEventHistory(String transactionId) {
        return eventStoreRepository.findByAggregateIdOrderByVersionAsc(transactionId);
    }

}
