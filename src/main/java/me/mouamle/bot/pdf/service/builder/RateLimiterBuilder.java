package me.mouamle.bot.pdf.service.builder;

import me.mouamle.bot.pdf.service.ConcurrentCache;
import me.mouamle.bot.pdf.service.RateLimiter;

import java.time.Duration;

public class RateLimiterBuilder {

    private String name;
    private int maxAttempts = 1;
    private boolean enableLogging = false;

    private int ttl;
    private int size = 1024;
    private int cleanUp = 1;

    public RateLimiterBuilder name(String name) {
        this.name = name;
        return this;
    }

    public RateLimiterBuilder maxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }

    public RateLimiterBuilder enableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
        return this;
    }

    public RateLimiterBuilder ttl(int ttl) {
        this.ttl = ttl;
        return this;
    }

    public RateLimiterBuilder size(int size) {
        this.size = size;
        return this;
    }

    public RateLimiterBuilder cleanUp(int cleanUp) {
        this.cleanUp = cleanUp;
        return this;
    }

    public <K> RateLimiter<K> build() {
        ConcurrentCache<K, Integer> botActionsCache = new ConcurrentCache<>(name, Duration.ofSeconds(ttl).toMillis(),
                Duration.ofSeconds(cleanUp).toMillis(), size);
        return new RateLimiter<>(name, maxAttempts, enableLogging, botActionsCache);
    }

}
