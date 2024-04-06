package me.fladoss.microservicesgamestore.inventory_service.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryRequest {
    private String skuCode;
    private Integer quantity;
}