package co.istad.transaction.command;

import lombok.Builder;

@Builder
public record FailTransactionCommand(
        String transactionId,
        String reason
) {
}
