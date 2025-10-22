package co.istad.transaction.repository;

import co.istad.transaction.domain.EventStore;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventStoreRepository extends
        MongoRepository<EventStore, String> {

    long countByAggregateId(String aggregateId);

}
