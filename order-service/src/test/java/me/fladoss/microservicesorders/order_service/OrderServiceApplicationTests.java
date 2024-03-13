package me.fladoss.microservicesorders.order_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.fladoss.microservicesorders.order_service.dto.OrderLineItemsDto;
import me.fladoss.microservicesorders.order_service.dto.OrderRequest;
import me.fladoss.microservicesorders.order_service.service.OrderService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Slf4j
class OrderServiceApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15.2"))
            .withDatabaseName("order_service_db_test");

    // adding mock application properties
    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.liquibase.change-log", () -> "classpath:/db/changelog/liquibase_order.changelog-test.yaml");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderService orderService;

    @Test
    void shouldCreateOrder() throws Exception {
        OrderRequest orderRequest = getOrderRequest();

        String orderRequestString = objectMapper.writeValueAsString(orderRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestString))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    void shouldGetOrder() throws Exception {
        mockMvc.perform((MockMvcRequestBuilders.get("/api/order/1")))
                .andExpect(MockMvcResultMatchers.status().isOk());
        Assertions.assertNotNull(orderService.getOrderById(1L));
        Assertions.assertEquals("game/The Talos Principle", orderService.getOrderById(1L).getOrderLineItems().get(0).getSkuCode());
    }

    @Test
    void shouldGetAllOrders() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/order"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        Assertions.assertTrue(orderService.getAllOrders().size() > 1);
    }

    private OrderRequest getOrderRequest() {
        List<OrderLineItemsDto> orderLineItemsDtoList = List.of(
                OrderLineItemsDto.builder()
                        .skuCode("game/The Talos Principle")
                        .quantity(1)
                        .price(BigDecimal.valueOf(6.49))
                        .build()
        );

        return OrderRequest.builder()
                .orderLineItemsDto(orderLineItemsDtoList)
                .build();
    }


}
