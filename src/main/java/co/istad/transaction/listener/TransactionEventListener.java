package co.istad.transaction.listener;

import co.istad.transaction.event.TransactionCompletedEvent;
import co.istad.transaction.event.TransactionCreatedEvent;
import co.istad.transaction.service.TransactionProjectionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionEventListener {

    private final TransactionProjectionService transactionProjectionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "banking.transaction.deposited", groupId = "${spring.application.name}")
    public void handleTransactionDepositedEvent(String event) {
        log.info("Received transaction deposited event {}", event);
        try {
            Class<?> eventClass = Class.forName(
                    "co.istad.transaction.event.TransactionCreatedEvent");
            Object deserializedObj = objectMapper.readValue(event, eventClass);
            transactionProjectionService.onProjection(deserializedObj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "banking.transaction.completed", groupId = "${spring.application.name}")
    public void handleTransactionCompletedEvent(String event) {
        log.info("Received transaction completed event: {}", event);
        try {
            Class<?> eventClass = Class.forName(
                    "co.istad.transaction.event.TransactionCompletedEvent");
            Object deserializedObj = objectMapper.readValue(event, eventClass);
            transactionProjectionService.onProjection(deserializedObj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "banking.transaction.failed", groupId = "${spring.application.name}")
    public void handleTransactionFailedEvent(String event) {
        log.info("Received transaction failed event: {}", event);
        try {
            Class<?> eventClass = Class.forName(
                    "co.istad.transaction.event.TransactionFailedEvent");
            Object deserializedObj = objectMapper.readValue(event, eventClass);
            transactionProjectionService.onProjection(deserializedObj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
