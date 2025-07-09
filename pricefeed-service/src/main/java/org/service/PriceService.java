package org.service;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.client.CoinMarketCapClient;
import org.dto.CoinMarketCapResponse;
import org.dto.PriceResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class PriceService {

    @Inject
    @RestClient
    CoinMarketCapClient coinMarketCapClient;

    @ConfigProperty(name = "coinmarketcap.api.key")
    String apiKey;

    @ConfigProperty(name = "coinmarketcap.default-currency", defaultValue = "USD")
    String defaultCurrency;

    @ConfigProperty(name = "coinmarketcap.limit", defaultValue = "100")
    int limit;

    // Track last fetch time for logging
    private volatile Instant lastFetchTime = Instant.MIN;
    private volatile List<PriceResponse> latestPrices;

    /**
     * Fetches fresh data from the API, bypassing cache
     */
    @Retry(maxRetries = 3, delay = 2000)
    @Timeout(4000)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.6, delay = 10000)
    public List<PriceResponse> fetchPricesFromCoinMarketCap() {
        Instant now = Instant.now();
        long secondsSinceLastFetch = ChronoUnit.SECONDS.between(lastFetchTime, now);

        log.info("Fetching prices from CoinMarketCap API. Time since last fetch: {} seconds", secondsSinceLastFetch);

        try {
            CoinMarketCapResponse response = coinMarketCapClient.getLatestListings(
                    apiKey,
                    1,  // start with first item
                    limit,
                    defaultCurrency
            );

            if (response.getStatus().getError_code() != 0) {
                log.error("API error: {}", response.getStatus().getError_message());
                throw new RuntimeException("CoinMarketCap API error: " + response.getStatus().getError_message());
            }

            List<PriceResponse> prices = mapToPriceResponses(response);
            this.latestPrices = prices;  // Cache locally
            this.lastFetchTime = now;

            log.info("Successfully fetched {} prices from API at {}", prices.size(), now);
            return prices;

        } catch (Exception e) {
            log.error("Error fetching prices from CoinMarketCap API", e);
            throw e;
        }
    }

    private List<PriceResponse> mapToPriceResponses(CoinMarketCapResponse response) {
        return response.getData().stream()
                .map(crypto -> {
                    Map<String, org.dto.CryptoCurrency.Quote> quoteMap = crypto.getQuote();
                    if (quoteMap == null) {
                        return null;
                    }

                    org.dto.CryptoCurrency.Quote quote = quoteMap.get(defaultCurrency);
                    if (quote == null) {
                        return null;
                    }

                    return PriceResponse.builder()
                            .id(String.valueOf(crypto.getId()))
                            .name(crypto.getName())
                            .symbol(crypto.getSymbol())
                            .price(quote.getPrice())
                            .currency(defaultCurrency)
                            .timestamp(Instant.now())
                            .marketCap(quote.getMarket_cap())
                            .volume24h(quote.getVolume_24h())
                            .percentChange1h(quote.getPercent_change_1h())
                            .percentChange24h(quote.getPercent_change_24h())
                            .percentChange7d(quote.getPercent_change_7d())
                            .percentChange30d(quote.getPercent_change_30d())
                            .rank(crypto.getCmc_rank())
                            .build();
                })
                .filter(price -> price != null)
                .collect(Collectors.toList());
    }

    /**
     * Gets prices, preferring cache if available
     */
    @CacheResult(cacheName = "price-cache")
    public List<PriceResponse> getCurrentPrices() {
        log.info("Cache miss for getCurrentPrices() - fetching from API");
        if (latestPrices == null) {
            return fetchPricesFromCoinMarketCap();
        }
        return latestPrices;
    }

    @CacheResult(cacheName = "price-cache")
    public PriceResponse getPriceById(String id) {
        log.info("Getting price for ID: {}", id);
        List<PriceResponse> prices = getCurrentPrices();
        return prices.stream()
                .filter(price -> price.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @CacheResult(cacheName = "price-cache")
    public PriceResponse getPriceBySymbol(String symbol) {
        log.info("Getting price for symbol: {}", symbol);
        List<PriceResponse> prices = getCurrentPrices();
        return prices.stream()
                .filter(price -> price.getSymbol().equalsIgnoreCase(symbol))
                .findFirst()
                .orElse(null);
    }

    /**
     * Force refresh cache and get new prices
     */
    @CacheInvalidateAll(cacheName = "price-cache")
    public List<PriceResponse> refreshPrices() {
        log.info("Manually refreshing price cache");
        return fetchPricesFromCoinMarketCap();
    }

    /**
     * Get cache info for debugging
     */
    public Map<String, Object> getCacheInfo() {
        return Map.of(
                "lastFetchTime", lastFetchTime.toString(),
                "secondsSinceLastFetch", ChronoUnit.SECONDS.between(lastFetchTime, Instant.now()),
                "cachedItemCount", latestPrices != null ? latestPrices.size() : 0
        );
    }
}