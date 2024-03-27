package me.fladoss.microservicesorders.order_service.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fladoss.microservicesorders.order_service.dto.InventoryResponse;
import me.fladoss.microservicesorders.order_service.dto.OrderLineItemsDto;
import me.fladoss.microservicesorders.order_service.dto.OrderRequest;
import me.fladoss.microservicesorders.order_service.dto.OrderResponse;
import me.fladoss.microservicesorders.order_service.entity.Order;
import me.fladoss.microservicesorders.order_service.entity.OrderLineItems;
import me.fladoss.microservicesorders.order_service.repository.OrderRepository;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    private final WebClient.Builder webClientBuilder;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return OrderResponse.builder()
                .id(id)
                .orderNumber(order.getOrderNumber())
                .orderLineItems(order.getOrderLineItems())
                .build();
    }

    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackPlaceOrder")
    @TimeLimiter(name = "inventory")
    @Retry(name = "inventory")
    public CompletableFuture<String> placeOrder(OrderRequest orderRequest) throws TimeoutException {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDto()
                .stream()
                .map(this::mapToObjectLineItems)
                .toList();
        order.setOrderLineItems(orderLineItems);

        for (OrderLineItems orderLineItem : orderLineItems) {
            orderLineItem.setOrder(order);
        }

        List<String> skuCodes = order.getOrderLineItems().stream().map(OrderLineItems::getSkuCode).toList();

        List<InventoryResponse> inventoryResponseList = webClientBuilder.build()
                .get()
                .uri(
                        "http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build()
                )
                .retrieve()
                .bodyToFlux(InventoryResponse.class)
                .collectList()
                .block();

        if (inventoryResponseList == null) {
            throw new IllegalStateException("No inventory response. InventoryResponseList is null");
        }
        if (inventoryResponseList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not in stock");
        }

        boolean allMatch = inventoryResponseList.stream()
                .allMatch(InventoryResponse::isInStock);

        if (!allMatch) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not in stock");
        }

        try {
            orderRepository.save(order);
        } catch (RuntimeException e) {
            log.error("Error saving order with id: {}", order.getId(), e);
            return CompletableFuture.completedFuture("Internal error occurred while saving order :(");
        }

        return CompletableFuture.completedFuture("Order placed successfully :)");
    }

    public void deleteOrder(Long id) {

        if (!orderRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        orderRepository.deleteById(id);
    }

    private OrderLineItems mapToObjectLineItems(OrderLineItemsDto orderLineItemsDto) {
        return OrderLineItems.builder()
                .skuCode(orderLineItemsDto.getSkuCode())
                .price(orderLineItemsDto.getPrice())
                .quantity(orderLineItemsDto.getQuantity())
                .build();
    }

    private CompletableFuture<String> fallbackPlaceOrder(OrderRequest orderRequest, Throwable throwable) {
        log.error("Fallback method called for placeOrder: ", throwable);
        return CompletableFuture.completedFuture("Internal error occurred while placing order :( \nTry again later.");
    }
}
