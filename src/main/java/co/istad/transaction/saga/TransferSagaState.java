package co.istad.transaction.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferSagaState {
    private String transactionId;
    private String currentStep;
    private String status;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private Boolean moneyReserved;
    private Boolean moneyCredited;
    private Boolean transferCompleted;
    private String remark;
}
