package com.oose.slsms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Smart Library Space Management System - Application entry point.
 *
 * Architecture: 3-tier MVC (Controller → Service → Repository)
 * Patterns used: State, Observer, Singleton, Strategy, Repository.
 */
@SpringBootApplication
public class SlsmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SlsmsApplication.class, args);
    }
}
