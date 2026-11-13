package ru.kotov.notification.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.kotov.notification.domain.ProcessedEvent;
import ru.kotov.notification.repository.ProcessedEventRepository;
import ru.kotov.notification.service.NotificationService;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final ProcessedEventRepository processedEventRepository;

    public OrderEventConsumer(ObjectMapper objectMapper,
                              NotificationService notificationService,
                              ProcessedEventRepository processedEventRepository) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(topics = "order.created", groupId = "notification-service")
    @Transactional
    public void onOrderCreated(String message) {
        processEvent("order.created", message);
    }

    @KafkaListener(topics = "order.confirmed", groupId = "notification-service")
    @Transactional
    public void onOrderConfirmed(String message) {
        processEvent("order.confirmed", message);
    }

    @KafkaListener(topics = "order.rejected", groupId = "notification-service")
    @Transactional
    public void onOrderRejected(String message) {
        processEvent("order.rejected", message);
    }

    private void processEvent(String topic, String message) {
        try {
            var event = objectMapper.readTree(message);
            String eventKey = event.get("orderId").asText();

            if (processedEventRepository.existsByTopicAndEventKey(topic, eventKey)) {
                log.info("Event already processed: topic={}, key={}", topic, eventKey);
                return;
            }

            Long orderId = event.get("orderId").asLong();
            String customerEmail = event.has("customerEmail") ? event.get("customerEmail").asText() : "unknown";
            String content = switch (topic) {
                case "order.created" -> "Order created: orderId=" + orderId + ", customer=" + customerEmail;
                case "order.confirmed" -> "Order confirmed: orderId=" + orderId;
                case "order.rejected" -> "Order rejected: orderId=" + orderId;
                default -> "Unknown event: " + topic;
            };

            notificationService.sendNotification(orderId, topic, customerEmail, content);

            processedEventRepository.save(new ProcessedEvent(topic, eventKey));
            log.info("Notification sent: topic={}, orderId={}", topic, orderId);
        } catch (Exception ex) {
            log.error("Failed to process event: topic={}, message={}", topic, message, ex);
        }
    }
}
