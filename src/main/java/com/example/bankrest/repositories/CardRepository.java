package com.example.bankrest.repositories;

import com.example.bankrest.entities.Card;
import com.example.bankrest.entities.User;
import com.example.bankrest.entities.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.bankrest.entities.Account;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByUuid(UUID uuid);
    Optional<Card> findByIdAndAccount(Long id, Account account);
    Page<Card> findByAccount(Account account, Pageable pageable);
    boolean existsByCardNumber(String cardNumber);
    Optional<Card> findByCardNumber(String cardNumber);
    Page<Card> findByAccount_User_Id(Long userId, Pageable pageable);
    boolean existsByAccountAndStatus(Account account, CardStatus status);
}
