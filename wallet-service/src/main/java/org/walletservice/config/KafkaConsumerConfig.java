package org.walletservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.walletservice.service.WalletService;

@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    @Autowired
    private WalletService walletService;

    @KafkaListener(topics = "user-registration", groupId = "wallet-service")
    public void consumeUserRegistration(String userId) {
        walletService.createWallet(userId);
    }
}