package co.istad.transaction.event;

import co.istad.transaction.domain.CurrencyEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequestedEvent {
    private String accountNumber;
    private BigDecimal amount;
    private CurrencyEnum currency;
    private String remark;
    private String transactionId;
}
