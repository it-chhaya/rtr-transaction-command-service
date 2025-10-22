package co.istad.transaction.controller;

import co.istad.transaction.dto.CreateDepositRequest;
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

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/deposit")
    public TransactionResponse createDeposit(
            @Valid @RequestBody CreateDepositRequest createDepositRequest
    ) {
        return transactionCommandService.createDeposit(createDepositRequest);
    }

}
