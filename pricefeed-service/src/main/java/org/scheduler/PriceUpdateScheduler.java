package org.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.dto.PriceResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kafka.PriceEventProducer;
import org.service.PriceService;

import java.util.List;

@ApplicationScoped
@Slf4j
public class PriceUpdateScheduler {

    @Inject
    PriceService priceService;

    @Inject
    PriceEventProducer priceEventProducer;

    @ConfigProperty(name = "price.update.interval", defaultValue = "60s")
    String updateInterval;

    @Scheduled(cron = "{price.update.cron:0 */1 * * * ?}")
    void schedulePriceUpdate() {
        log.info("Scheduled price update started");
        try {
            // Force cache refresh and get new prices
            List<PriceResponse> prices = priceService.refreshPrices();

            // Publish to Kafka
            if (prices != null && !prices.isEmpty()) {
                priceEventProducer.publishPriceUpdates(prices);
                log.info("Published {} price updates to Kafka", prices.size());
            } else {
                log.warn("No prices available to publish");
            }
        } catch (Exception e) {
            log.error("Error during scheduled price update: {}", e.getMessage(), e);
        }
    }
}