package co.istad.transaction.service;

import co.istad.transaction.dto.CreateDepositRequest;
import co.istad.transaction.dto.TransactionResponse;

public interface TransactionCommandService {

    TransactionResponse createDeposit(CreateDepositRequest createDepositRequest);

}
