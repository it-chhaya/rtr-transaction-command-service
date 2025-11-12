package co.istad.transaction.command;

import co.istad.transaction.domain.CurrencyEnum;

import java.math.BigDecimal;

public record DepositCommand(
        String accountNumber,
        BigDecimal amount,
        CurrencyEnum currency,
        String remark
) {
}
