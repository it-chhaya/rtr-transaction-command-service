package co.istad.transaction.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TransactionResponse(
        String transactionId,
        BigDecimal amount
) {
}
