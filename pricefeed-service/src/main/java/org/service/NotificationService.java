package org.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.client.NotificationClient;
import org.dto.NotificationRequest;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.dto.PriceResponse;


import java.util.List;

@ApplicationScoped
@Slf4j
public class NotificationService {

    @Inject
    @RestClient
    NotificationClient notificationClient;

    public void sendPriceAlerts(List<PriceResponse> significantMovers) {
        log.info("Sending alerts for {} significant price movements", significantMovers.size());

        significantMovers.forEach(price -> {
            try {
                NotificationRequest request = NotificationRequest.builder()
                        .type("PRICE_MOVEMENT")
                        .priority(determinePriority(price))
                        .subject("Significant price movement for " + price.getSymbol())
                        .content(buildNotificationContent(price))
                        .build();

                notificationClient.sendNotification(request);
                log.debug("Alert sent for {}", price.getSymbol());
            } catch (Exception e) {
                log.error("Failed to send alert for {}", price.getSymbol(), e);
            }
        });
    }

    private String determinePriority(PriceResponse price) {
        if (price.getPercentChange24h() != null) {
            double change = price.getPercentChange24h().doubleValue();
            if (Math.abs(change) > 20) return "HIGH";
            if (Math.abs(change) > 10) return "MEDIUM";
        }
        return "LOW";
    }

    private String buildNotificationContent(PriceResponse price) {
        StringBuilder content = new StringBuilder();
        content.append(price.getName())
                .append(" (")
                .append(price.getSymbol())
                .append(") has moved ");

        if (price.getPercentChange24h() != null) {
            content.append(String.format("%.2f", price.getPercentChange24h()))
                    .append("% in the last 24 hours. ");
        }

        content.append("Current price: $")
                .append(String.format("%.2f", price.getPrice()));

        return content.toString();
    }
}