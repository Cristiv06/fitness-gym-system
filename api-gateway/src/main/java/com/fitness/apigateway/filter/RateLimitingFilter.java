package com.fitness.apigateway.filter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Limitare de rata in-memory, per IP client, cu fereastra fixa.
 * Peste capacitatea configurata raspunde cu 429 Too Many Requests.
 */
@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private final int capacity;
    private final long windowSeconds;
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    public RateLimitingFilter(
            @Value("${gateway.rate-limit.capacity:30}") int capacity,
            @Value("${gateway.rate-limit.window-seconds:10}") long windowSeconds) {
        this.capacity = capacity;
        this.windowSeconds = windowSeconds;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientKey = resolveClientKey(exchange);
        long nowSec = Instant.now().getEpochSecond();
        long window = nowSec - (nowSec % windowSeconds);

        boolean[] limited = {false};
        int[] used = {0};
        counters.compute(clientKey, (key, counter) -> {
            if (counter == null || counter.windowStart != window) {
                counter = new Counter(window);
            }
            counter.count++;
            if (counter.count > capacity) {
                limited[0] = true;
            }
            used[0] = counter.count;
            return counter;
        });

        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add("X-RateLimit-Limit", String.valueOf(capacity));
        response.getHeaders().add("X-RateLimit-Remaining", String.valueOf(Math.max(0, capacity - used[0])));

        if (limited[0]) {
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            response.getHeaders().add("Content-Type", "application/json");
            response.getHeaders().add("Retry-After", String.valueOf(windowSeconds));
            byte[] body = ("{\"message\":\"Rate limit depasit. Reincearca in cateva secunde.\",\"status\":429}")
                    .getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(body);
            return response.writeWith(Mono.just(buffer));
        }

        return chain.filter(exchange);
    }

    private String resolveClientKey(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private static final class Counter {
        private final long windowStart;
        private int count;

        private Counter(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
