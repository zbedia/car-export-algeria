package com.carexport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CarExportBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarExportBackendApplication.class, args);
    }

}
