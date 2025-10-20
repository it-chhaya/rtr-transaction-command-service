package co.istad.transaction;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "eventStores")
public class EventStore {
    @Id
    private String id;
    private UUID eventId;
    private String version;
    private String eventType;
    private String aggregateId;
    private String aggregateType;
    private Instant timestamp = Instant.now();
    private Object eventData;
}
