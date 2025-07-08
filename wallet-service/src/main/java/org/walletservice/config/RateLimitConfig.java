package org.walletservice.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableCaching
public class RateLimitConfig {

    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();

    public enum LimitTier {
        STANDARD(10),
        PREMIUM(50),
        BUSINESS(200);
        private final int operationsPerMinute;
        LimitTier(int operationsPerMinute) {
            this.operationsPerMinute = operationsPerMinute;
        }
        public int getOperationsPerMinute() {
            return operationsPerMinute;
        }
    }

    public Bucket createNewBucket(LimitTier limitTier) {
        Refill refill = Refill.intervally(limitTier.getOperationsPerMinute(), Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(limitTier.getOperationsPerMinute(), refill);
        return Bucket.builder().addLimit(limit).build();
    }

    public Bucket resolveBucket(String userId, LimitTier tier) {
        return userBuckets.computeIfAbsent(userId + ":" + tier.name(), key -> createNewBucket(tier));
    }

    public Bucket resolveQueryBucket(String userId) {
        return resolveBucket(userId, LimitTier.PREMIUM);
    }

    public Bucket resolveTransferBucket(String userId) {
        return resolveBucket(userId, LimitTier.STANDARD);
    }

    public Bucket resolveWithdrawalBucket(String userId) {
        return resolveBucket(userId, LimitTier.STANDARD);
    }
}