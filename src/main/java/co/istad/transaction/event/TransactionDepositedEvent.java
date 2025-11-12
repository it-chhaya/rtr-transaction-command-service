package co.istad.transaction.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDepositedEvent {
    private String accountNumber;
    private BigDecimal amount;
    private BigDecimal balance;
    private String txnId;
}
