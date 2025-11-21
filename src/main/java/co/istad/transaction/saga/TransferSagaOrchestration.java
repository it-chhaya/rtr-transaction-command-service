package co.istad.transaction.saga;

import co.istad.transaction.command.CreateTransferCommand;
import co.istad.transaction.command.ReserveMoneyCommand;
import co.istad.transaction.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransferSagaOrchestration {

    private final EventPublisher eventPublisher;

    private final ConcurrentHashMap<String, TransferSagaState> sagaStates
            = new ConcurrentHashMap<>();

    public void startTransferSaga(String transactionId, CreateTransferCommand command) {
        log.info("start transfer saga: {}", command);

        // Create SAGA state
        TransferSagaState state = TransferSagaState.builder()
                .transactionId(transactionId)
                .fromAccountNumber(command.fromAccountNumber())
                .toAccountNumber(command.toAccountNumber())
                .amount(command.amount())
                .remark(command.remark())
                .build();
        sagaStates.put(transactionId, state);

        ReserveMoneyCommand reserveMoneyCommand = ReserveMoneyCommand.builder()
                .transactionId(state.getTransactionId())
                .accountNumber(state.getFromAccountNumber())
                .amount(command.amount())
                .build();

        eventPublisher.publishEvent(
                "reserve-money-command",
                transactionId,
                reserveMoneyCommand
        );
    }


    // Happy event
    @KafkaListener(topics = "money-reserved-event", groupId = "${spring.application.name}")
    public void handleReservedMoney(String event) {
        log.info("handle reserved-money: {}", event);
    }


    // Unhappy event
    @KafkaListener(topics = "money-reserve-failed-event", groupId = "${spring.application.name}")
    public void handleReserveMoneyFailed(String event) {
        log.info("handle reserve-money-failed-event: {}", event);
    }


}
