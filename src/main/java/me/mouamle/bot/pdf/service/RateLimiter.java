package me.mouamle.bot.pdf.service;

import lombok.extern.slf4j.Slf4j;
import me.mouamle.bot.pdf.service.builder.RateLimiterBuilder;

@Slf4j
public class RateLimiter<K> {

    private final String name;
    private final int maxAttempts;

    private final boolean enableLogging;
    private final ConcurrentCache<K, Integer> cache;

    public RateLimiter(String name, int maxAttempts, boolean enableLogging, ConcurrentCache<K, Integer> cache) {
        this.name = name;
        this.maxAttempts = maxAttempts;
        this.enableLogging = enableLogging;
        this.cache = cache;
    }

    public String getName() {
        return name;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public boolean action(K key) {
        if (!cache.containsKey(key)) {
            cache.put(key, 0);
        }

        Integer attempts = cache.peek(key);
        if (attempts >= maxAttempts) {
            if (enableLogging) {
                log.info("entry {} reached max attempts of {} for rate limiter {}", key, maxAttempts, name);
            } else {
                log.trace("entry {} reached max attempts of {} for rate limiter {}", key, maxAttempts, name);
            }
            return false;
        }

        cache.put(key, attempts + 1);
        return true;
    }

    public static RateLimiterBuilder builder() {
        return new RateLimiterBuilder();
    }

}
