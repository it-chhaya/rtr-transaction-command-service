package co.istad.transaction.repository;

import co.istad.transaction.domain.EventStore;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventStoreRepository extends
        MongoRepository<EventStore, String> {

    long countByAggregateId(String aggregateId);

    List<EventStore> findByAggregateIdOrderByVersionAsc(String aggregateId);
}
