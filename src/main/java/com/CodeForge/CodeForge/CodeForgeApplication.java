package com.CodeForge.CodeForge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class CodeForgeApplication {

    public static void main(String[] args) {
        // Load .env variables
        Dotenv dotenv = Dotenv.load(); // looks for .env in project root

        // Set system properties so Spring Boot can pick them up
        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USER", dotenv.get("DB_USER"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

        SpringApplication.run(CodeForgeApplication.class, args);
    }
}
