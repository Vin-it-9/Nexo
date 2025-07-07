package org.walletservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PriceFeedService {

    private final Map<String, BigDecimal> priceCache = new HashMap<>();

    public PriceFeedService() {

        priceCache.put("BTC", new BigDecimal("50000.00"));
        priceCache.put("ETH", new BigDecimal("3000.00"));
        priceCache.put("USDT", new BigDecimal("1.00"));
        priceCache.put("USDC", new BigDecimal("1.00"));
        priceCache.put("BNB", new BigDecimal("350.00"));
        priceCache.put("SOL", new BigDecimal("150.00"));
        priceCache.put("ADA", new BigDecimal("1.20"));
        priceCache.put("XRP", new BigDecimal("0.75"));
        priceCache.put("DOT", new BigDecimal("25.00"));
        priceCache.put("DOGE", new BigDecimal("0.15"));
    }

    public BigDecimal getCurrentPrice(String symbol) {
        log.debug("Fetching price for {}", symbol);
        return priceCache.getOrDefault(symbol, BigDecimal.ONE);
    }

    public BigDecimal updatePrice(String symbol, BigDecimal price) {
        log.info("Updating price for {} to {}", symbol, price);
        priceCache.put(symbol, price);
        return price;
    }
}