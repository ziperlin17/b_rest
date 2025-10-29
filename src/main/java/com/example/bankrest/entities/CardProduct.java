package com.example.bankrest.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "card_products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", unique = true, nullable = false)
    private String productName;

    @Column(name = "payment_system", nullable = false)
    private String paymentSystem;
}