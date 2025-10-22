package co.istad.transaction.dto;

import co.istad.transaction.domain.CurrencyEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateDepositRequest(
        @NotNull
        @Positive
        Long accountId,
        @NotNull
        BigDecimal amount,
        @NotNull
        CurrencyEnum currency,
        String remark
) {
}
