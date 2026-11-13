package ru.kotov.inventory.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kotov.inventory.domain.Product;
import ru.kotov.inventory.dto.ReservationRequest;
import ru.kotov.inventory.repository.ProductRepository;

@Service
@Transactional(readOnly = true)
public class InventoryService {

    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public boolean reserveProduct(ReservationRequest request) {
        Product product = productRepository.findBySkuWithAvailable(request.getSku(), request.getQuantity())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product not available: sku=" + request.getSku() + ", requested=" + request.getQuantity()));

        product.setReserved(product.getReserved() + request.getQuantity());
        productRepository.save(product);
        return true;
    }

    @Transactional
    public void releaseReservation(String sku, Integer quantity) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with sku: " + sku));

        product.setReserved(Math.max(0, product.getReserved() - quantity));
        productRepository.save(product);
    }
}
