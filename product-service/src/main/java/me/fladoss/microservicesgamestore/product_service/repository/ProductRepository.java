package me.fladoss.microservicesgamestore.product_service.repository;

import me.fladoss.microservicesgamestore.product_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

}