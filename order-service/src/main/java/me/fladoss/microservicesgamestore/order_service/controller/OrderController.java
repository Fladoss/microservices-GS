package me.fladoss.microservicesgamestore.order_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fladoss.microservicesgamestore.order_service.dto.OrderRequest;
import me.fladoss.microservicesgamestore.order_service.dto.OrderResponse;
import me.fladoss.microservicesgamestore.order_service.entity.Order;
import me.fladoss.microservicesgamestore.order_service.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        try {
            return ResponseEntity.ok(orderService.getAllOrders());
        } catch (RuntimeException e) {
            log.error("Error while getting all orders", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderService.getOrderById(id));
        } catch (RuntimeException e) {
            log.error("Error while getting an order with id {}", id);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error while deleting order with id {}", id);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest orderRequest) {
        try {
            String result = orderService.placeOrder(orderRequest).get();
            return ResponseEntity.created(URI.create("/api/order")).body(result);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error while executing placing of an order", e);
            return ResponseEntity.internalServerError().body("Internal error while placing an order");
        } catch (TimeoutException e) {
            log.error("Timed out while placing an order", e);
            return ResponseEntity.internalServerError().body("Timed out while placing an order. Try again later!");
        } catch (RuntimeException e) {
            log.error("Unexpected error while placing an order", e);
            return ResponseEntity.internalServerError().body("Unexpected error while placing an order");
        }
    }
}
