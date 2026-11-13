package ru.kotov.orders.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kotov.orders.domain.Order;
import ru.kotov.orders.domain.OutboxEvent;
import ru.kotov.orders.dto.OrderCreatedEvent;
import ru.kotov.orders.dto.OrderRequest;
import ru.kotov.orders.dto.OrderResponse;
import ru.kotov.orders.messaging.KafkaTopicConfig;
import ru.kotov.orders.repository.OrderRepository;
import ru.kotov.orders.repository.OutboxRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OrderService(OrderRepository orderRepository,
                        OutboxRepository outboxRepository,
                        ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        if (orderRepository.existsByOrderNumber(request.getOrderNumber())) {
            throw new IllegalArgumentException("Order with number " + request.getOrderNumber() + " already exists");
        }

        Order order = new Order(
                request.getOrderNumber(),
                request.getCustomerName(),
                request.getCustomerEmail(),
                request.getTotalAmount()
        );
        Order saved = orderRepository.save(order);

        // Сохраняем событие в outbox в той же транзакции (Transactional Outbox)
        try {
            String payload = objectMapper.writeValueAsString(new OrderCreatedEvent(saved.getId(),
                    saved.getOrderNumber(), saved.getCustomerEmail(), saved.getTotalAmount()));
            OutboxEvent outboxEvent = new OutboxEvent(
                    "Order",
                    saved.getId().toString(),
                    "OrderCreated",
                    payload
            );
            outboxRepository.save(outboxEvent);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize order created event", ex);
        }

        return new OrderResponse(saved);
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));
        return new OrderResponse(order);
    }

    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with number: " + orderNumber));
        return new OrderResponse(order);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));

        if (order.getStatus() != Order.Status.NEW) {
            throw new IllegalStateException("Only orders with status NEW can be cancelled, current: " + order.getStatus());
        }

        order.setStatus(Order.Status.CANCELLED);
        orderRepository.save(order);
    }

    @Transactional
    public void confirmOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));
        order.setStatus(Order.Status.CONFIRMED);
        orderRepository.save(order);
    }

    @Transactional
    public void rejectOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));
        order.setStatus(Order.Status.REJECTED);
        orderRepository.save(order);
    }
}
