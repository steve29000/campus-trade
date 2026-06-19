package com.campustrade.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class CampusOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusOrderApplication.class, args);
    }
}
