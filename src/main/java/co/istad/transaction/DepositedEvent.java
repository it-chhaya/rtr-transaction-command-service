package co.istad.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositedEvent {
    private String accountId;
    private String transactionId;
    private BigDecimal amount;
}
