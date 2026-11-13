package ru.kotov.orders.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kotov.orders.domain.OutboxEvent;
import ru.kotov.orders.repository.OutboxRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxRelayService {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayService.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxRelayService(OutboxRepository outboxRepository,
                              KafkaTemplate<String, String> kafkaTemplate,
                              ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:5000}")
    @Transactional
    public void relayUnpublishedEvents() {
        List<OutboxEvent> unpublished = outboxRepository.findUnpublished();
        if (unpublished.isEmpty()) {
            return;
        }

        for (OutboxEvent event : unpublished) {
            try {
                String topic = resolveTopic(event.getEventType());
                String key = event.getAggregateId();

                kafkaTemplate.send(topic, key, event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to send outbox event id={} topic={}", event.getId(), topic, ex);
                            } else {
                                log.debug("Outbox event id={} sent to topic={}", event.getId(), topic);
                            }
                        });

                outboxRepository.markAsPublished(event.getId(), LocalDateTime.now());
                log.info("Outbox event id={} published to topic={}", event.getId(), topic);
            } catch (Exception ex) {
                log.error("Error processing outbox event id={}", event.getId(), ex);
            }
        }
    }

    private String resolveTopic(String eventType) {
        return switch (eventType) {
            case "OrderCreated" -> KafkaTopicConfig.ORDER_CREATED_TOPIC;
            case "OrderConfirmed" -> KafkaTopicConfig.ORDER_CONFIRMED_TOPIC;
            case "OrderRejected" -> KafkaTopicConfig.ORDER_REJECTED_TOPIC;
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
}
