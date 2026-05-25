package com.bigdata.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.bigdata.admin.mapper")
public class BigDataAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(BigDataAdminApplication.class, args);
        System.out.println("=================================");
        System.out.println("Big Data Admin Platform Started!");
        System.out.println("API: http://localhost:8080/api");
        System.out.println("Swagger: http://localhost:8080/api/swagger-ui.html");
        System.out.println("=================================");
    }
}
