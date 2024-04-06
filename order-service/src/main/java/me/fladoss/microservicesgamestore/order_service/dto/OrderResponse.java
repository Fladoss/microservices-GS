package me.fladoss.microservicesgamestore.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.fladoss.microservicesgamestore.order_service.entity.OrderLineItems;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private List<OrderLineItems> orderLineItems;
}
