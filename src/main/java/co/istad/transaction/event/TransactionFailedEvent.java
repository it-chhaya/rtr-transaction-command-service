package co.istad.transaction.event;

import co.istad.transaction.domain.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionFailedEvent {
    private String transactionId;
    private TransactionStatus status;
    private String remark;
}
