package me.fladoss.microservicesgamestore.order_service.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fladoss.microservicesgamestore.order_service.dto.InventoryResponse;
import me.fladoss.microservicesgamestore.order_service.dto.OrderLineItemsDto;
import me.fladoss.microservicesgamestore.order_service.dto.OrderRequest;
import me.fladoss.microservicesgamestore.order_service.dto.OrderResponse;
import me.fladoss.microservicesgamestore.order_service.entity.Order;
import me.fladoss.microservicesgamestore.order_service.entity.OrderLineItems;
import me.fladoss.microservicesgamestore.order_service.event.OrderPlacedEvent;
import me.fladoss.microservicesgamestore.order_service.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
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

    private final Tracer tracer;

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

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

        Span span = tracer.nextSpan().name("inventory-service-observation");

        try (Tracer.SpanInScope withSpan = tracer.withSpan(span.start())) {
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
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "inventoryResponseList is null");
            }

            boolean allMatch = inventoryResponseList.stream()
                    .allMatch(InventoryResponse::isInStock);

            if (allMatch) {
                try {
                    orderRepository.save(order);
                    kafkaTemplate.send("orderServiceNotification", new OrderPlacedEvent(order.getOrderNumber()));
                    return CompletableFuture.completedFuture("Order placed successfully :)");
                } catch (RuntimeException e) {
                    log.error("Error saving order with id: {}", order.getId(), e);
                    return CompletableFuture.completedFuture("Internal error occurred while saving order :(");
                } finally {
                    span.end();
                }
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not in stock");
            }
        }
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
        log.error("Fallback method is called now for order with items: ({}); in placeOrder method: {}", orderRequest.getOrderLineItemsDto(), throwable.getStackTrace());
        return CompletableFuture.completedFuture("Internal error occurred while placing order :( \n");
    }
}
