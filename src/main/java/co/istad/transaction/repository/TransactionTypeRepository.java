package co.istad.transaction.repository;

import co.istad.transaction.domain.TransactionType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionTypeRepository
extends MongoRepository<TransactionType, String> {
}
