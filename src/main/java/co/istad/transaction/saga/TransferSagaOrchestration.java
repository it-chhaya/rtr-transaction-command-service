package co.istad.transaction.saga;

import co.istad.transaction.command.*;
import co.istad.transaction.domain.TransactionStatus;
import co.istad.transaction.publisher.EventPublisher;
import co.istad.transaction.service.TransactionCommandService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
    private final ObjectMapper objectMapper;

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
    public void handleReservedMoney(ConsumerRecord<String, String> record) {
        log.info("handle reserved-money: {}", record.key());

        TransferSagaState sagaState = sagaStates.get(record.key());
        sagaState.setMoneyReserved(true);
        sagaState.setCurrentStep("CREDIT_MONEY");
        sagaState.setStatus(TransactionStatus.IN_PROGRESS.name());
        log.info("load saga state: {}", sagaState);

        CreditMoneyCommand creditMoneyCommand = CreditMoneyCommand.builder()
                .transactionId(sagaState.getTransactionId())
                .accountNumber(sagaState.getToAccountNumber())
                .amount(sagaState.getAmount())
                .build();
        eventPublisher.publishEvent("credit-money-command",
                creditMoneyCommand.transactionId(),
                creditMoneyCommand);
    }


    @KafkaListener(topics = "money-credited-event", groupId = "${spring.application.name}")
    public void handleCreditedMoney(ConsumerRecord<String, String> record) {
        log.info("handle credited-money: {}", record.key());

        TransferSagaState sagaState = sagaStates.get(record.key());
        sagaState.setMoneyCredited(true);
        sagaState.setCurrentStep("COMPLETE_TRANSFER");
        sagaState.setStatus(TransactionStatus.IN_PROGRESS.name());

        CompleteTransactionCommand completeTransactionCommand = CompleteTransactionCommand.builder()
                .transactionId(sagaState.getTransactionId())
                .build();

        eventPublisher.publishEvent("complete-transfer-command",
                completeTransactionCommand.transactionId(),
                completeTransactionCommand);
    }


    @KafkaListener(topics = "transfer-completed-event", groupId = "${spring.application.name}")
    public void handleTransferCompleted(ConsumerRecord<String, String> record) {
        log.info("handle transfer-completed-event");
        TransferSagaState sagaState = sagaStates.get(record.key());
        sagaState.setTransferCompleted(true);
        sagaState.setCurrentStep("FINISHED");
        sagaState.setStatus(TransactionStatus.COMPLETED.name());

        // Clean SAGA state
        sagaStates.remove(record.key());
    }


    // Unhappy event
    @KafkaListener(topics = "money-reserve-failed-event", groupId = "${spring.application.name}")
    public void handleReserveMoneyFailed(ConsumerRecord<String, String> record) {
        log.info("handle reserve-money-failed-event: {}", record.key());

        FailTransactionCommand failTransactionCommand;
        TransferSagaState sagaState;

        try {
            failTransactionCommand = objectMapper.readValue(record.value(),
                    FailTransactionCommand.class);
        } catch (JsonProcessingException e) {
            failTransactionCommand = FailTransactionCommand.builder()
                    .transactionId(record.key())
                    .reason("Reserve money failed from account")
                    .build();
        }
        sagaState = sagaStates.get(failTransactionCommand.transactionId());
        sagaState.setStatus(TransactionStatus.FAILED.name());
        sagaState.setRemark(failTransactionCommand.reason());

        eventPublisher.publishEvent("fail-transfer-command",
                sagaState.getTransactionId(),
                failTransactionCommand);

        sagaStates.remove(record.key());
    }


    @KafkaListener(topics = "money-credit-failed-event", groupId = "${spring.application.name}")
    public void handleMoneyCreditFailed(ConsumerRecord<String, String> record) {
        log.info("handle money credit failed: {}", record.key());

        FailTransactionCommand failTransactionCommand;
        TransferSagaState sagaState;

        try {
            failTransactionCommand = objectMapper.readValue(record.value(),
                    FailTransactionCommand.class);
        } catch (JsonProcessingException e) {
            failTransactionCommand = FailTransactionCommand.builder()
                    .transactionId(record.key())
                    .reason("Credit money failed from account")
                    .build();
        }
        sagaState = sagaStates.get(failTransactionCommand.transactionId());
        sagaState.setCurrentStep("CANCEL_RESERVATION");
        sagaState.setStatus(TransactionStatus.COMPENSATION.name());
        sagaState.setRemark(failTransactionCommand.reason());

        CancelReservationCommand cancelReservationCommand = CancelReservationCommand.builder()
                .transactionId(sagaState.getTransactionId())
                .accountNumber(sagaState.getFromAccountNumber())
                .amount(sagaState.getAmount())
                .reason(failTransactionCommand.reason())
                .build();

        eventPublisher.publishEvent("cancel-reservation-command",
                sagaState.getTransactionId(),
                cancelReservationCommand);
    }


    @KafkaListener(topics = "reservation-cancelled-event", groupId = "${spring.application.name}")
    public void handleReservationCancelled(ConsumerRecord<String, String> record) {
        log.info("handle reservation-cancelled: {}", record.key());

        FailTransactionCommand failTransactionCommand;
        TransferSagaState sagaState;

        try {
            failTransactionCommand = objectMapper.readValue(record.value(),
                    FailTransactionCommand.class);
        } catch (JsonProcessingException e) {
            failTransactionCommand = FailTransactionCommand.builder()
                    .transactionId(record.key())
                    .reason("Cancel reservation failed from account")
                    .build();
        }
        sagaState = sagaStates.get(failTransactionCommand.transactionId());
        sagaState.setCurrentStep("FINISHED");
        sagaState.setStatus(TransactionStatus.COMPENSATED.name());
        sagaState.setRemark(failTransactionCommand.reason());

        eventPublisher.publishEvent("fail-transfer-command",
                sagaState.getTransactionId(),
                failTransactionCommand);

        sagaStates.remove(record.key());
    }


}
