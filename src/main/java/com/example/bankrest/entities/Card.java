package com.example.bankrest.entities;

import com.example.bankrest.entities.enums.CardStatus;
import com.example.bankrest.util.AesEncryptor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cards", indexes = {
        @Index(name = "idx_card_uuid", columnList = "uuid", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"account", "cvvEncrypted"})
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, unique = true)
    private UUID uuid;

    @Convert(converter = AesEncryptor.class)
    @Column(name = "card_number_encrypted", unique = true, nullable = false)
    private String cardNumber;

    @Column(name = "cardholder_name", nullable = false)
    private String cardholderName;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Convert(converter = AesEncryptor.class)
    @Column(name = "cvv_encrypted", nullable = false)
    private String cvv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_product_id", nullable = false)
    private CardProduct cardProduct;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }
}