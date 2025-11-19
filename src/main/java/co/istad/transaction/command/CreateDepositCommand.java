package co.istad.transaction.command;

import co.istad.transaction.domain.CurrencyEnum;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateDepositCommand(
        @NotNull
        String accountNumber,
        @NotNull
        BigDecimal amount,
        @NotNull
        CurrencyEnum currency,
        String remark
) {
}
