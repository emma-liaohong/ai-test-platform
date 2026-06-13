package com.aitest;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.aitest.modules.*.mapper")
public class AiTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiTestApplication.class, args);
    }
}
