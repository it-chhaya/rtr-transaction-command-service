package co.istad.transaction.command;

import lombok.Builder;

@Builder
public record CompleteTransactionCommand(
        String transactionId
) {
}
