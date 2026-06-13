package com.fitness.gymservice.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demonstreaza refresh dinamic al configuratiei fara restart.
 *
 * Flux demo:
 *  1. GET  http://localhost:8082/api/config-demo  -> vezi valoarea curenta
 *  2. Editezi gym.info.welcome-message in config-server/config-repo/gym-service.yml
 *  3. POST http://localhost:8082/actuator/refresh  -> serviciul reincarca configul
 *  4. GET  http://localhost:8082/api/config-demo  -> vezi valoarea noua (fara restart)
 *
 * @RefreshScope face ca bean-ul sa fie reconstruit la /actuator/refresh,
 * citind din nou proprietatile @Value din Config Server.
 */
@RestController
@RequestMapping("/api/config-demo")
@RefreshScope
public class ConfigDemoController {

    @Value("${gym.info.welcome-message}")
    private String welcomeMessage;

    @Value("${gym.info.max-default-capacity}")
    private int maxDefaultCapacity;

    @GetMapping
    public Map<String, Object> info() {
        return Map.of(
                "welcomeMessage", welcomeMessage,
                "maxDefaultCapacity", maxDefaultCapacity);
    }
}
