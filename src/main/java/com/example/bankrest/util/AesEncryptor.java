package com.example.bankrest.util;

import com.example.bankrest.exception.EncryptionException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

@Component
@Converter
@Slf4j
public class AesEncryptor implements AttributeConverter<String, String> {
    private static final String AES = "AES";
    private final Key key;
    private final Cipher encryptionCipher;
    private final Cipher decryptionCipher;

    public AesEncryptor(@Value("${app.encryption.aes.key}") String secretKey) {
        if (secretKey == null || (secretKey.length() != 16 && secretKey.length() != 24 && secretKey.length() != 32)) {
            log.error("Invalid AES key length. Key must be 16, 24, or 32 bytes long.");
            throw new IllegalArgumentException("Invalid AES key length.");
        }
        try {
            this.key = new SecretKeySpec(secretKey.getBytes(), AES);
            this.encryptionCipher = Cipher.getInstance(AES);
            this.encryptionCipher.init(Cipher.ENCRYPT_MODE, key);
            this.decryptionCipher = Cipher.getInstance(AES);
            this.decryptionCipher.init(Cipher.DECRYPT_MODE, key);
        } catch (Exception e) {
            log.error("Error initializing AES ciphers", e);
            throw new IllegalStateException("Failed to initialize AES ciphers", e);
        }
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            byte[] encryptedBytes = encryptionCipher.doFinal(attribute.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Error encrypting attribute", e);
            throw new EncryptionException("Could not encrypt data", e);
        }
    }
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(dbData);
            return new String(decryptionCipher.doFinal(decodedBytes));
        } catch (Exception e) {
            log.error("Error decrypting attribute", e);
            throw new EncryptionException("Could not decrypt data", e);
        }
    }
}