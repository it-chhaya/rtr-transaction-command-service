package co.istad.transaction.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishEvent(String topicName, Object event) {
        try {
            String eventJsonStr = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topicName, eventJsonStr)
                    .whenComplete((res, ex) -> {
                        if (ex == null) {
                            log.info("Published event {} into topic {} successfully", event, topicName);
                        } else {
                            log.error("Published event {} into topic {} failed", event, ex.getMessage(), ex);
                        }
                    });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void publishEvents(String topicName, List<Object> events) {
        events.forEach(event -> this.publishEvent(topicName, event));
    }

}
