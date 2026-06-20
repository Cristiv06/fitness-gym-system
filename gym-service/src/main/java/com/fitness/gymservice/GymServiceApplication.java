package com.fitness.gymservice;

import com.fitness.gymservice.config.LoadBalancerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@LoadBalancerClients(defaultConfiguration = LoadBalancerConfig.class)
public class GymServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GymServiceApplication.class, args);
    }
}
