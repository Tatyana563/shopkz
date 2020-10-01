package com.example.shopkz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShopkzApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopkzApplication.class, args);
    }

}
