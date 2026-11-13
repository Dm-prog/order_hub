package ru.kotov.orders.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.kotov.orders.domain.Order;
import ru.kotov.orders.service.OrderService;

@Component
public class OrderStatusConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusConsumer.class);

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public OrderStatusConsumer(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopicConfig.ORDER_CONFIRMED_TOPIC, groupId = "order-service")
    public void onOrderConfirmed(String message) {
        try {
            var event = objectMapper.readTree(message);
            Long orderId = event.get("orderId").asLong();
            orderService.confirmOrder(orderId);
            log.info("Order confirmed: orderId={}", orderId);
        } catch (Exception ex) {
            log.error("Failed to process OrderConfirmed event: {}", message, ex);
        }
    }

    @KafkaListener(topics = KafkaTopicConfig.ORDER_REJECTED_TOPIC, groupId = "order-service")
    public void onOrderRejected(String message) {
        try {
            var event = objectMapper.readTree(message);
            Long orderId = event.get("orderId").asLong();
            orderService.rejectOrder(orderId);
            log.info("Order rejected: orderId={}", orderId);
        } catch (Exception ex) {
            log.error("Failed to process OrderRejected event: {}", message, ex);
        }
    }
}
