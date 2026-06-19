package com.campustrade.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class CampusProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusProductApplication.class, args);
    }
}
