package com.example.bankrest.repositories;

import com.example.bankrest.entities.CardProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardProductRepository extends JpaRepository<CardProduct, Long> {
//     Optional<CardProduct> findByProductName(String productName);
}
