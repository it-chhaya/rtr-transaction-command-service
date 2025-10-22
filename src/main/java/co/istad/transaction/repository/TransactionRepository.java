package co.istad.transaction.repository;

import co.istad.transaction.domain.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository
extends MongoRepository<Transaction, String> {

}
