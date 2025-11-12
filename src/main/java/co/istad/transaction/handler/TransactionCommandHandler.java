package co.istad.transaction.handler;

import co.istad.transaction.command.DepositCommand;
import co.istad.transaction.saga.TransactionSagaOrchestration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionCommandHandler {

    private final TransactionSagaOrchestration saga;

    public String handleDepositCommand(DepositCommand command) {
        return saga.initiateDeposit(
                command.accountNumber(),
                command.amount(),
                command.currency(),
                command.remark()
        );
    }

}
