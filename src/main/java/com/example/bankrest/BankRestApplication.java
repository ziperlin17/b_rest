package com.example.bankrest;

import com.example.bankrest.config.properties.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(JwtProperties.class)
@SpringBootApplication
public class BankRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankRestApplication.class, args);
    }

}
