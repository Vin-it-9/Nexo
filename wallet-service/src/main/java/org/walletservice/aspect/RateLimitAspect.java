package org.walletservice.aspect;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.walletservice.config.RateLimitConfig;
import org.walletservice.dto.ErrorResponse;

@Aspect
@Component
public class RateLimitAspect {

    private final RateLimitConfig rateLimitConfig;

    public RateLimitAspect(RateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
    }

    @Around("@annotation(org.walletservice.annotation.RateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication != null ? authentication.getName() : "anonymous";
        String methodName = joinPoint.getSignature().getName().toLowerCase();
        Bucket bucket;

        if (methodName.contains("transfer")) {
            bucket = rateLimitConfig.resolveTransferBucket(userId);
        } else if (methodName.contains("withdraw")) {
            bucket = rateLimitConfig.resolveWithdrawalBucket(userId);
        } else {
            bucket = rateLimitConfig.resolveQueryBucket(userId);
        }

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            return joinPoint.proceed();
        } else {
            long waitTimeSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            ErrorResponse errorResponse = new ErrorResponse(
                    "Rate limit exceeded",
                    "Too many requests. Please try again after " + waitTimeSeconds + " seconds.",
                    HttpStatus.TOO_MANY_REQUESTS.value()
            );
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitTimeSeconds))
                    .body(errorResponse);
        }
    }
}