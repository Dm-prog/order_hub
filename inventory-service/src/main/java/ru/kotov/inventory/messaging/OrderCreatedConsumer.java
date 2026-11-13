package ru.kotov.inventory.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.kotov.inventory.domain.ProcessedEvent;
import ru.kotov.inventory.dto.ReservationRequest;
import ru.kotov.inventory.repository.ProcessedEventRepository;
import ru.kotov.inventory.service.InventoryService;

@Component
public class OrderCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedConsumer.class);

    private final ObjectMapper objectMapper;
    private final InventoryService inventoryService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ProcessedEventRepository processedEventRepository;

    public OrderCreatedConsumer(ObjectMapper objectMapper,
                                InventoryService inventoryService,
                                KafkaTemplate<String, String> kafkaTemplate,
                                ProcessedEventRepository processedEventRepository) {
        this.objectMapper = objectMapper;
        this.inventoryService = inventoryService;
        this.kafkaTemplate = kafkaTemplate;
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(topics = KafkaTopicConfig.ORDER_CREATED_TOPIC, groupId = "inventory-service")
    @Transactional
    public void onOrderCreated(String message) {
        try {
            var event = objectMapper.readTree(message);
            String topic = KafkaTopicConfig.ORDER_CREATED_TOPIC;
            String eventKey = event.get("orderId").asText();

            // Идемпотентность: проверяем, не обработано ли уже это событие
            if (processedEventRepository.existsByTopicAndEventKey(topic, eventKey)) {
                log.info("Event already processed: topic={}, key={}", topic, eventKey);
                return;
            }

            Long orderId = event.get("orderId").asLong();
            String sku = event.has("sku") ? event.get("sku").asText() : "DEFAULT-SKU";
            Integer quantity = event.has("quantity") ? event.get("quantity").asInt() : 1;

            try {
                inventoryService.reserveProduct(new ReservationRequest(
                        String.valueOf(orderId), sku, quantity));

                String confirmedPayload = objectMapper.writeValueAsString(new OrderConfirmedEvent(orderId));
                kafkaTemplate.send(KafkaTopicConfig.ORDER_CONFIRMED_TOPIC, String.valueOf(orderId), confirmedPayload);

                processedEventRepository.save(new ProcessedEvent(topic, eventKey));
                log.info("Order created processed: orderId={}, reserved", orderId);
            } catch (Exception ex) {
                String rejectedPayload = objectMapper.writeValueAsString(new OrderRejectedEvent(orderId, ex.getMessage()));
                kafkaTemplate.send(KafkaTopicConfig.ORDER_REJECTED_TOPIC, String.valueOf(orderId), rejectedPayload);

                processedEventRepository.save(new ProcessedEvent(topic, eventKey));
                log.warn("Order created processed: orderId={}, rejected: {}", orderId, ex.getMessage());
            }
        } catch (Exception ex) {
            log.error("Failed to process OrderCreated event: {}", message, ex);
        }
    }

    public record OrderConfirmedEvent(Long orderId) {}
    public record OrderRejectedEvent(Long orderId, String reason) {}
}
