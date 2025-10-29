package com.example.bankrest.service.impl;
import com.example.bankrest.util.GeneratedCardDetails;
import com.example.bankrest.repositories.CardRepository;
import com.example.bankrest.service.CardGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardGenerationServiceImpl implements CardGenerationService {

    private static final String CARD_BIN_PREFIX = "1337";
    private static final int CARD_NUMBER_LENGTH = 16;
    private static final int CARD_EXPIRATION_YEARS = 5;

    private final CardRepository cardRepository;
    private final Random random = new SecureRandom();

    @Override
    public GeneratedCardDetails generateNewCardDetails() {
        String cardNumber;
        do {
            cardNumber = generateLuhnCompliantNumber();
        } while (cardRepository.existsByCardNumber(cardNumber));

        String cvv = generateCvv();
        LocalDate expirationDate = calculateExpirationDate();

        log.info("Successfully generated new card details.");
        return new GeneratedCardDetails(cardNumber, cvv, expirationDate);
    }

    private String generateLuhnCompliantNumber() {
        int payloadLength = CARD_NUMBER_LENGTH - CARD_BIN_PREFIX.length() - 1;
        StringBuilder numberBuilder = new StringBuilder(CARD_BIN_PREFIX);
        for (int i = 0; i < payloadLength; i++) {
            numberBuilder.append(random.nextInt(10));
        }
        int checkDigit = calculateLuhnCheckDigit(numberBuilder.toString());
        numberBuilder.append(checkDigit);
        return numberBuilder.toString();
    }

    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum * 9) % 10;
    }

    private String generateCvv() {
        int cvvNumber = random.nextInt(900) + 100;
        return String.valueOf(cvvNumber);
    }

    private LocalDate calculateExpirationDate() {
        return LocalDate.now().plusYears(CARD_EXPIRATION_YEARS)
                .with(TemporalAdjusters.lastDayOfMonth());
    }
}