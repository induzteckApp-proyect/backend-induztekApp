package com.induztek.activos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ActivosApplication {
    public static void main(String[] args) {
        SpringApplication.run(ActivosApplication.class, args);
    }
}
