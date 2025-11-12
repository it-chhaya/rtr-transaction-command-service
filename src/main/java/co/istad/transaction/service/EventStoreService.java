package co.istad.transaction.service;

import co.istad.transaction.aggregate.TransactionAggregate;
import co.istad.transaction.domain.EventStore;

import java.util.List;

public interface EventStoreService {

    void saveEvents(TransactionAggregate aggregate, String correlationId);

    TransactionAggregate loadAggregate(String transactionId);

    List<EventStore> getEventHistory(String transactionId);

}
