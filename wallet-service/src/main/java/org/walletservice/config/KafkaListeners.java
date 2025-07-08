package org.walletservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.walletservice.dto.kafka.PriceUpdateEvent;
import org.walletservice.dto.kafka.UserRegistrationEvent;
import org.walletservice.service.PriceFeedService;
import org.walletservice.service.WalletService;

import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaListeners {

    @Autowired
    private WalletService walletService;
    @Autowired
    private PriceFeedService priceFeedService;

    @KafkaListener(topics = "user-registration", groupId = "wallet-service")
    public void consumeUserRegistration(UserRegistrationEvent event) {
        try {
            walletService.createWallet(event.getUserId());
        } catch (Exception e) {
            Logger.getLogger(KafkaListeners.class.getName()).warning(e.getMessage());
        }
    }

    @KafkaListener(topics = "price-updates", groupId = "wallet-service")
    public void consumePriceUpdate(PriceUpdateEvent event) {
        try {
            priceFeedService.updatePrice(event.getSymbol(), event.getPrice());
        } catch (Exception e) {
            log.error("Error updating price for {}", event.getSymbol(), e);
        }
    }
}