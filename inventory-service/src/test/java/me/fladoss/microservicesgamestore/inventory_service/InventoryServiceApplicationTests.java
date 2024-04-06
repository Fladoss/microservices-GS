package me.fladoss.microservicesgamestore.inventory_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.fladoss.microservicesgamestore.inventory_service.dto.InventoryRequest;
import me.fladoss.microservicesgamestore.inventory_service.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Slf4j
class InventoryServiceApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.2")
            .withDatabaseName("inventory_service_db_test");

    // adding mock application properties
    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.liquibase.change-log", () -> "classpath:/db/changelog/liquibase_inventory.changelog-test.yaml");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InventoryService inventoryService;

    @Test
    void shouldCreateInventory() throws Exception {
        InventoryRequest inventoryRequest = getInventoryRequestPost();

        String inventoryRequestString = objectMapper.writeValueAsString(inventoryRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inventoryRequestString))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

//    @Test
//    void shouldBeInStock() throws Exception {
//        mockMvc.perform((MockMvcRequestBuilders.get("/api/inventory/GAME_PC-The_Talos_Principle")))
//                .andExpect(MockMvcResultMatchers.status().isOk());
//        Assertions.assertTrue(inventoryService.isInStock("GAME_PC-The_Talos_Principle"));
//    }

//    @Test
//    void shouldUpdateInventory() throws Exception {
//        String productRequestString = objectMapper.writeValueAsString(getInventoryRequestPut());
//
//        mockMvc.perform(MockMvcRequestBuilders.put("/api/inventory/0")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(productRequestString))
//                .andExpect(MockMvcResultMatchers.status().isCreated());
//        Assertions.assertTrue(inventoryService.isInStock("GAME_PC-Outward-2"));
//    }

    private InventoryRequest getInventoryRequestPost() {
        return InventoryRequest.builder()
                .quantity(100)
                .skuCode("GAME_PC-Outward")
                .build();
    }

    private InventoryRequest getInventoryRequestPut() {
        return InventoryRequest.builder()
                .quantity(1)
                .skuCode("GAME_PC-Outward-2")
                .build();
    }
}
