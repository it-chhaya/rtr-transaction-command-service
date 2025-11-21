package co.istad.transaction.controller;

import co.istad.transaction.command.CreateDepositCommand;
import co.istad.transaction.command.CreateTransferCommand;
import co.istad.transaction.dto.TransactionResponse;
import co.istad.transaction.service.TransactionCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionCommandService transactionCommandService;


    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/transfer")
    public String createTransfer(
            @RequestBody CreateTransferCommand command
            ) {
        return transactionCommandService
                .createTransfer(command);
    }


    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/deposit")
    public String createDeposit(
            @Valid @RequestBody CreateDepositCommand createDepositCommand
    ) {
        return transactionCommandService
                .createDeposit(createDepositCommand);
    }

}
