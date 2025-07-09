package org.service;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class CacheEvictionService {

    @Inject
    @CacheName("price-cache")
    Cache priceCache;

    @Scheduled(every = "10m")
    void invalidatePriceCache() {
        priceCache.invalidateAll().await().indefinitely();
    }
}