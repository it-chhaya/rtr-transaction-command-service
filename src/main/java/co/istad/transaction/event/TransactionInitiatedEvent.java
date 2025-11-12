package co.istad.transaction.event;

import co.istad.transaction.domain.CurrencyEnum;
import co.istad.transaction.domain.TransactionType;
import co.istad.transaction.domain.TypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionInitiatedEvent {
    private String transactionId;
    private TypeEnum type;
    private String accountNumber;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private CurrencyEnum currency;
    private String remark;
    private Instant createdAt;
}
