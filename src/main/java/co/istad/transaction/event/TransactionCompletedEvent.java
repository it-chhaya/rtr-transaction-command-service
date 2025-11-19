package co.istad.transaction.event;

import co.istad.transaction.domain.CurrencyEnum;
import co.istad.transaction.domain.TransactionStatus;
import co.istad.transaction.domain.TypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCompletedEvent {
    private String transactionId;
    private TransactionStatus status;
}
