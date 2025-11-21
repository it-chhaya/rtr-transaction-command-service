package co.istad.transaction.service;

import co.istad.transaction.command.CompleteTransactionCommand;
import co.istad.transaction.command.CreateDepositCommand;
import co.istad.transaction.command.CreateTransferCommand;
import co.istad.transaction.command.FailTransactionCommand;

public interface TransactionCommandService {

    String createTransfer(CreateTransferCommand command);

    String createDeposit(CreateDepositCommand createDepositCommand);

    void handleCompleteTransactionCommand(CompleteTransactionCommand command);

    void handleFailTransactionCommand(FailTransactionCommand command);
}
