package co.istad.transaction.listener;

import co.istad.transaction.command.CompleteTransactionCommand;
import co.istad.transaction.command.FailTransactionCommand;
import co.istad.transaction.event.TransactionCompletedEvent;
import co.istad.transaction.publisher.EventPublisher;
import co.istad.transaction.service.TransactionCommandService;
import co.istad.transaction.service.TransactionProjectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionEventListener {

    private final TransactionCommandService transactionCommandService;
    private final TransactionProjectionService transactionProjectionService;
    private final ObjectMapper objectMapper;
    private final EventPublisher eventPublisher;

    @KafkaListener(topics = {
            "banking.transaction.deposited",
            "transaction-created-event"
    }, groupId = "${spring.application.name}")
    public void handleTransactionCreatedEvent(String event) {
        log.info("Received transaction completed event {}", event);
        try {
            Class<?> eventClass = Class.forName(
                    "co.istad.transaction.event.TransactionCreatedEvent");
            Object deserializedObj = objectMapper.readValue(event, eventClass);
            transactionProjectionService.onProjection(deserializedObj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @KafkaListener(topics = {
            "fail-transfer-command"
    }, groupId = "${spring.application.name}")
    public void handleFailTransferEvent(ConsumerRecord<String, String> event) {
        log.info("Received transfer failed event: {}", event);
        try {
            FailTransactionCommand command = objectMapper.readValue(event.value(), FailTransactionCommand.class);
            transactionCommandService.handleFailTransactionCommand(command);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @KafkaListener(topics = {
            "complete-transfer-command"
    }, groupId = "${spring.application.name}")
    public void handleCompleteTransferEvent(ConsumerRecord<String, String> event) {
        log.info("Received transfer completed event: {}", event);
        try {
            CompleteTransactionCommand command = objectMapper.readValue(event.value(), CompleteTransactionCommand.class);
            transactionCommandService.handleCompleteTransactionCommand(command);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @KafkaListener(topics = {
            "banking.transaction.completed",
    }, groupId = "${spring.application.name}")
    public void handleTransactionCompletedEvent(String event) {
        log.info("Received transaction completed event: {}", event);
        try {
            TransactionCompletedEvent deserializedObj = objectMapper.readValue(event, TransactionCompletedEvent.class);
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
