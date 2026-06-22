package com.campustrade.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.campustrade.user.mapper")
@SpringBootApplication
public class CampusUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusUserApplication.class, args);
    }
}
