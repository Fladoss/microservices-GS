package me.fladoss.microservicesgamestore.product_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.fladoss.microservicesgamestore.product_service.dto.ProductRequest;
import me.fladoss.microservicesgamestore.product_service.service.ProductService;
import org.junit.jupiter.api.Assertions;
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

import java.math.BigDecimal;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Slf4j
class ProductServiceApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.2")
            .withDatabaseName("product_service_db_test");

    // adding mock application properties
    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.liquibase.change-log", () -> "classpath:/db/changelog/liquibase_product.changelog-test.yaml");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductService productService;

    @Test
    void shouldCreateProduct() throws Exception {
        ProductRequest productRequest = getProductRequest();

        String productRequestString = objectMapper.writeValueAsString(productRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequestString))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    void shouldGetProduct() throws Exception {
        mockMvc.perform((MockMvcRequestBuilders.get("/api/product/0")))
                .andExpect(MockMvcResultMatchers.status().isOk());
        Assertions.assertNotNull(productService.getProductById(0L));
        Assertions.assertEquals("The Talos Principle", productService.getProductById(0L).getName());
    }

    @Test
    void shouldGetAllProducts() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/product"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        Assertions.assertTrue(productService.getAllProducts().size() > 1);
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        String productRequestString = objectMapper.writeValueAsString(getProductRequest());
        log.info("Result string: " + productRequestString);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/product/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequestString))
                .andExpect(MockMvcResultMatchers.status().isCreated());
        Assertions.assertEquals("Outward", productService.getProductById(1L).getName());
    }

    private ProductRequest getProductRequest() {
        return ProductRequest.builder()
                .name("Outward")
                .description("Bla-bla-bla...")
                .price(BigDecimal.valueOf(39.99))
                .build();
    }
}
