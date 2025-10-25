package com.example.bankrest.repositories;

import com.example.bankrest.entities.Card;
import com.example.bankrest.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findByUser(User user, Pageable pageable);
    Optional<Card> findByCardNumber(String cardNumber);
    Optional<Card> findByIdAndUser(Long id, User user);
}
