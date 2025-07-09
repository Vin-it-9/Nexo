package org.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.dto.PriceResponse;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class MarketAnalysisService {

    @Inject
    PriceService priceService;

    public List<PriceResponse> getTopGainers(int limit) {
        List<PriceResponse> prices = priceService.getCurrentPrices();
        return prices.stream()
                .filter(p -> p.getPercentChange24h() != null)
                .sorted(Comparator.comparing(PriceResponse::getPercentChange24h).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<PriceResponse> getTopLosers(int limit) {
        List<PriceResponse> prices = priceService.getCurrentPrices();
        return prices.stream()
                .filter(p -> p.getPercentChange24h() != null)
                .sorted(Comparator.comparing(PriceResponse::getPercentChange24h))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Map<String, Double> getMarketCapDistribution() {
        List<PriceResponse> prices = priceService.getCurrentPrices();

        double totalMarketCap = prices.stream()
                .filter(p -> p.getMarketCap() != null)
                .mapToDouble(p -> p.getMarketCap().doubleValue())
                .sum();

        Map<String, Double> distribution = new HashMap<>();

        prices.stream()
                .filter(p -> p.getMarketCap() != null)
                .sorted(Comparator.comparing(PriceResponse::getMarketCap).reversed())
                .limit(10)
                .forEach(p -> {
                    double percentage = (p.getMarketCap().doubleValue() / totalMarketCap) * 100;
                    distribution.put(p.getSymbol(), percentage);
                });

        double topTenSum = distribution.values().stream().mapToDouble(Double::doubleValue).sum();
        distribution.put("Others", 100 - topTenSum);

        return distribution;
    }

    public List<PriceResponse> getVolumeLeaders(int limit) {
        List<PriceResponse> prices = priceService.getCurrentPrices();
        return prices.stream()
                .filter(p -> p.getVolume24h() != null)
                .sorted(Comparator.comparing(PriceResponse::getVolume24h).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public boolean createPriceAlert(String symbol, double threshold, String direction) {
        log.info("Creating price alert for {} at {} {}", symbol, threshold, direction);
        return true;
    }
}