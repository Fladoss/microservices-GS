package me.fladoss.microservicesgamestore.inventory_service.controller;

import lombok.RequiredArgsConstructor;
import me.fladoss.microservicesgamestore.inventory_service.dto.InventoryRequest;
import me.fladoss.microservicesgamestore.inventory_service.dto.InventoryResponse;
import me.fladoss.microservicesgamestore.inventory_service.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam List<String> skuCode) {
        return inventoryService.isInStock(skuCode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createInventory(@RequestBody InventoryRequest inventoryRequest) {
        inventoryService.createInventory(inventoryRequest);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public void updateInventory(@PathVariable Long id,
                                @RequestBody InventoryRequest inventoryRequest) {
        inventoryService.updateInventory(id, inventoryRequest);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void deleteInventory(Long id) {
        inventoryService.deleteInventory(id);
    }
}
