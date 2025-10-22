package co.istad.transaction.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactionTypes")
public class TransactionType {
    @Id
    private String id;
    private TypeEnum typeCode;
    private String description;
}
