package com.fitness.apigateway.filter;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filtrare request/response centralizata:
 * - injecteaza un X-Gateway-Request-Id pe request (catre serviciile din spate)
 * - adauga X-Gateway si X-Response-Time-Ms pe response (catre client)
 * - logheaza metoda, calea si latenta fiecarui apel rutat prin gateway.
 */
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = UUID.randomUUID().toString();
        long start = System.currentTimeMillis();

        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-Gateway-Request-Id", requestId)
                .build();
        ServerWebExchange mutated = exchange.mutate().request(request).build();

        log.info("[GATEWAY] -> {} {} (reqId={})",
                request.getMethod(), request.getURI().getRawPath(), requestId);

        mutated.getResponse().beforeCommit(() -> {
            long elapsed = System.currentTimeMillis() - start;
            mutated.getResponse().getHeaders().add("X-Gateway", "fitness-api-gateway");
            mutated.getResponse().getHeaders().add("X-Response-Time-Ms", String.valueOf(elapsed));
            log.info("[GATEWAY] <- {} {} status={} {}ms (reqId={})",
                    request.getMethod(), request.getURI().getRawPath(),
                    mutated.getResponse().getStatusCode(), elapsed, requestId);
            return Mono.empty();
        });

        return chain.filter(mutated);
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
