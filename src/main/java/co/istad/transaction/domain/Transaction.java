package co.istad.transaction.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.expression.spel.ast.TypeCode;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;

    private String transactionId;
    private String accountNumber;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private TypeEnum typeCode;
    private CurrencyEnum currency;
    private String remark;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    private TypeEnum type;
    private TransactionStatus status;

    private Long version;

}
