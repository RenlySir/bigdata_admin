package com.bigdata.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@MapperScan("com.bigdata.admin.mapper")
public class BigDataAdminApplication {

    private static final Logger log = LoggerFactory.getLogger(BigDataAdminApplication.class);

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    public static void main(String[] args) {
        SpringApplication.run(BigDataAdminApplication.class, args);
    }

    /**
     * Log startup information after application is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("=================================");
        log.info("Big Data Admin Platform Started!");
        log.info("API: http://localhost:{}{}", serverPort, contextPath);
        log.info("Swagger: http://localhost:{}{}swagger-ui.html", serverPort, contextPath);
        log.info("=================================");
    }
}
