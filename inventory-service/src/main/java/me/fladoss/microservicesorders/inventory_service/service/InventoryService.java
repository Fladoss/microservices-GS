package me.fladoss.microservicesorders.inventory_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fladoss.microservicesorders.inventory_service.dto.InventoryRequest;
import me.fladoss.microservicesorders.inventory_service.dto.InventoryResponse;
import me.fladoss.microservicesorders.inventory_service.entity.Inventory;
import me.fladoss.microservicesorders.inventory_service.repository.InventoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCodes) {
        return inventoryRepository.findBySkuCodeIn(skuCodes).stream()
                .map(inventory -> InventoryResponse.builder()
                        .skuCode(inventory.getSkuCode())
                        .isInStock(inventory.getQuantity() > 0)
                        .build())
                .toList();
    }

    @Transactional
    public void createInventory(@RequestBody InventoryRequest inventoryRequest) {
        Inventory inventory = Inventory.builder()
                .quantity(inventoryRequest.getQuantity())
                .skuCode(inventoryRequest.getSkuCode())
                .build();

        inventoryRepository.save(inventory);
    }

    @Transactional
    public void updateInventory(Long id, InventoryRequest inventoryRequest) {
        log.info("Searching for a product with id {} before UPDATE", id);

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        log.info("Inventory with id {} found! Building a new inventory before creating...", id);

        inventory.setQuantity(inventoryRequest.getQuantity());
        inventory.setSkuCode(inventoryRequest.getSkuCode());
        inventoryRepository.save(inventory);

        log.info("Inventory with id {} created successfully", inventory.getId());
    }

    @Transactional
    public void deleteInventory(Long id) {
        if (!inventoryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        inventoryRepository.deleteById(id);

        log.info("Inventory with id {} deleted successfully", id);
    }
}