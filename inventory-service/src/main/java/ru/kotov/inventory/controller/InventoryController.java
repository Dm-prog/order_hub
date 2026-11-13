package ru.kotov.inventory.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kotov.inventory.dto.ReservationRequest;
import ru.kotov.inventory.service.InventoryService;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserve(@Valid @RequestBody ReservationRequest request) {
        inventoryService.reserveProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
