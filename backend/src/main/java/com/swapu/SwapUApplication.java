package com.swapu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@MapperScan("com.swapu.mapper")
public class SwapUApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwapUApplication.class, args);
    }

}
