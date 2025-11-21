package co.istad.transaction.command;

import co.istad.transaction.domain.CurrencyEnum;

import java.math.BigDecimal;

public record CreateTransferCommand(
        String fromAccountNumber,
        String toAccountNumber,
        BigDecimal amount,
        CurrencyEnum currency,
        String remark
) {
}
