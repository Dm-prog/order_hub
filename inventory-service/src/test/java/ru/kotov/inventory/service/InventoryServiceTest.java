package ru.kotov.inventory.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kotov.inventory.domain.Product;
import ru.kotov.inventory.dto.ReservationRequest;
import ru.kotov.inventory.repository.ProductRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void reserveProduct_shouldReserveWhenAvailable() {
        // given
        Product product = new Product("SKU-001", "Widget", 100);
        product.setReserved(10);
        // available = 90

        ReservationRequest request = new ReservationRequest("ORD-001", "SKU-001", 5);

        when(productRepository.findBySkuWithAvailable("SKU-001", 5)).thenReturn(Optional.of(product));

        // when
        boolean result = inventoryService.reserveProduct(request);

        // then
        assertThat(result).isTrue();
        assertThat(product.getReserved()).isEqualTo(15);
        verify(productRepository).save(product);
    }

    @Test
    void reserveProduct_shouldThrowWhenNotAvailable() {
        // given
        Product product = new Product("SKU-001", "Widget", 5);
        product.setReserved(5);
        // available = 0

        ReservationRequest request = new ReservationRequest("ORD-001", "SKU-001", 1);

        when(productRepository.findBySkuWithAvailable("SKU-001", 1)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> inventoryService.reserveProduct(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void releaseReservation_shouldDecreaseReserved() {
        // given
        Product product = new Product("SKU-001", "Widget", 100);
        product.setReserved(50);

        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(product));

        // when
        inventoryService.releaseReservation("SKU-001", 20);

        // then
        assertThat(product.getReserved()).isEqualTo(30);
        verify(productRepository).save(product);
    }
}
