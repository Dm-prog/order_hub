package ru.kotov.orders.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kotov.orders.domain.Order;
import ru.kotov.orders.domain.OutboxEvent;
import ru.kotov.orders.dto.OrderRequest;
import ru.kotov.orders.dto.OrderResponse;
import ru.kotov.orders.repository.OrderRepository;
import ru.kotov.orders.repository.OutboxRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_shouldSaveOrderAndOutboxEvent() throws Exception {
        // given
        OrderRequest request = new OrderRequest();
        request.setOrderNumber("ORD-001");
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");
        request.setTotalAmount(new BigDecimal("100.00"));

        Order savedOrder = new Order("ORD-001", "John Doe", "john@example.com", new BigDecimal("100.00"));
        savedOrder.setId(1L);

        when(orderRepository.existsByOrderNumber("ORD-001")).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"orderId\":1}");

        // when
        OrderResponse response = orderService.createOrder(request);

        // then
        assertThat(response.getOrderNumber()).isEqualTo("ORD-001");
        assertThat(response.getStatus()).isEqualTo("NEW");
        verify(orderRepository).save(any(Order.class));
        verify(outboxRepository).save(any(OutboxEvent.class));
    }

    @Test
    void createOrder_shouldThrowWhenOrderNumberExists() {
        // given
        OrderRequest request = new OrderRequest();
        request.setOrderNumber("ORD-001");
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");
        request.setTotalAmount(new BigDecimal("100.00"));

        when(orderRepository.existsByOrderNumber("ORD-001")).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(orderRepository, never()).save(any());
        verify(outboxRepository, never()).save(any());
    }

    @Test
    void getOrderById_shouldReturnOrderWhenExists() {
        // given
        Order order = new Order("ORD-001", "John Doe", "john@example.com", new BigDecimal("100.00"));
        order.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // when
        OrderResponse response = orderService.getOrderById(1L);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getOrderNumber()).isEqualTo("ORD-001");
    }

    @Test
    void getOrderById_shouldThrowWhenNotFound() {
        // given
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void cancelOrder_shouldChangeStatusToCancelled() {
        // given
        Order order = new Order("ORD-001", "John Doe", "john@example.com", new BigDecimal("100.00"));
        order.setId(1L);
        order.setStatus(Order.Status.NEW);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // when
        orderService.cancelOrder(1L);

        // then
        assertThat(order.getStatus()).isEqualTo(Order.Status.CANCELLED);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_shouldThrowWhenNotNewStatus() {
        // given
        Order order = new Order("ORD-001", "John Doe", "john@example.com", new BigDecimal("100.00"));
        order.setId(1L);
        order.setStatus(Order.Status.CONFIRMED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // when / then
        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only orders with status NEW can be cancelled");
    }

    @Test
    void confirmOrder_shouldChangeStatusToConfirmed() {
        // given
        Order order = new Order("ORD-001", "John Doe", "john@example.com", new BigDecimal("100.00"));
        order.setId(1L);
        order.setStatus(Order.Status.NEW);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // when
        orderService.confirmOrder(1L);

        // then
        assertThat(order.getStatus()).isEqualTo(Order.Status.CONFIRMED);
    }

    @Test
    void rejectOrder_shouldChangeStatusToRejected() {
        // given
        Order order = new Order("ORD-001", "John Doe", "john@example.com", new BigDecimal("100.00"));
        order.setId(1L);
        order.setStatus(Order.Status.NEW);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // when
        orderService.rejectOrder(1L);

        // then
        assertThat(order.getStatus()).isEqualTo(Order.Status.REJECTED);
    }
}
