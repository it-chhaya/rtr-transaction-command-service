package co.istad.transaction.controller;

import co.istad.transaction.command.DepositCommand;
import co.istad.transaction.handler.TransactionCommandHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionCommandHandler transactionCommandHandler;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/deposit")
    public ResponseEntity<?> createDeposit(
            @Valid @RequestBody DepositCommand depositCommand
    ) {
        String transactionId = transactionCommandHandler.handleDepositCommand(depositCommand);

        Map<String, String> response = new HashMap<>();
        response.put("transactionId", transactionId);
        response.put("status", "INITIATED");
        response.put("message", "Deposit initiated successfully");

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

}
