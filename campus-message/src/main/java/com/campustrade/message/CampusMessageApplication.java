package com.campustrade.message;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class CampusMessageApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusMessageApplication.class, args);
    }
}
