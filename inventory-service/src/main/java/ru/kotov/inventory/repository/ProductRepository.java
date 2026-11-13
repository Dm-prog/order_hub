package ru.kotov.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.kotov.inventory.domain.Product;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    @Query("SELECT p FROM Product p WHERE p.sku = :sku AND p.quantity - p.reserved >= :quantity")
    Optional<Product> findBySkuWithAvailable(@Param("sku") String sku, @Param("quantity") Integer quantity);
}
