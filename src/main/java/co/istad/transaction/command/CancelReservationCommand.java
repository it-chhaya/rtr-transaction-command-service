package co.istad.transaction.command;

import lombok.Builder;

import java.math.BigDecimal;

// Debit balance from account
@Builder
public record CancelReservationCommand(
        String transactionId,
        String accountNumber,
        BigDecimal amount,
        String reason
) {
}
