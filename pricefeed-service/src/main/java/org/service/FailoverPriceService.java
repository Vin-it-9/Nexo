package org.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.client.AlternativeApiClient;
import org.dto.PriceResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
@Slf4j
public class FailoverPriceService {

    @Inject
    PriceService primaryPriceService;

    @Inject
    @RestClient
    AlternativeApiClient alternativeApiClient;

    @ConfigProperty(name = "alternative.api.key")
    String alternativeApiKey;

    @ConfigProperty(name = "price.data.failover.enabled", defaultValue = "true")
    boolean failoverEnabled;

    @Fallback(fallbackMethod = "getAlternativePrices")
    public List<PriceResponse> getPricesWithFailover() {
        return primaryPriceService.getCurrentPrices();
    }

    public List<PriceResponse> getAlternativePrices() {
        if (!failoverEnabled) {
            log.warn("Failover requested but not enabled. Returning empty list.");
            return List.of();
        }
        log.info("Primary price source failed, using alternative API");
        try {
            return alternativeApiClient.getPrices(alternativeApiKey);
        } catch (Exception e) {
            log.error("Alternative API also failed", e);
            return List.of();
        }
    }
}