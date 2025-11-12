package co.istad.transaction.listener;

import co.istad.transaction.domain.EventStore;
import co.istad.transaction.saga.TransactionSagaOrchestration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventListener {

    private final TransactionSagaOrchestration saga;

    @KafkaListener(topics = "deposit-completed", groupId = "${spring.application.name}")
    public void handleDepositCompleted(Map<String, Object> event) {
        log.info("Received deposit completed event: {}", event);
        String transactionId = (String) event.get("transactionId");
        saga.handleDepositCompleted(transactionId);
    }

    @KafkaListener(topics = "deposit-failed", groupId = "${spring.application.name}")
    public void handleDepositFailed(Map<String, Object> event) {
        log.info("Received deposit failed event {}", event);
        String transactionId = (String) event.get("transactionId");
        String reason = (String) event.get("reason");
        saga.handleDepositFailed(transactionId, reason);
    }

}
