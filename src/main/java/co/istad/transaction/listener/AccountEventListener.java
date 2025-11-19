package co.istad.transaction.listener;

import co.istad.transaction.command.CompleteTransactionCommand;
import co.istad.transaction.command.FailTransactionCommand;
import co.istad.transaction.service.TransactionCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventListener {

    private final TransactionCommandService transactionCommandService;

    @KafkaListener(topics = "deposit-completed", groupId = "${spring.application.name}")
    public void handleDepositedEvent(Map<String, Object> event) {
        log.info("handle deposit event: {}", event);
        CompleteTransactionCommand command = CompleteTransactionCommand.builder()
                .transactionId((String) event.get("transactionId"))
                .build();
        transactionCommandService.handleCompleteTransactionCommand(command);
    }

    @KafkaListener(topics = "deposit-failed", groupId = "${spring.application.name}")
    public void handleDepositFailedEvent(Map<String, Object> event) {
        log.info("handle deposit failed event: {}", event);
        FailTransactionCommand command = FailTransactionCommand.builder()
                .transactionId((String) event.get("transactionId"))
                .reason((String) event.get("reason"))
                .build();
        transactionCommandService.handleFailTransactionCommand(command);
    }



}
