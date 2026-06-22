package com.campustrade.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.campustrade.order.mapper")
@EnableFeignClients
@SpringBootApplication
public class CampusOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusOrderApplication.class, args);
    }
}
