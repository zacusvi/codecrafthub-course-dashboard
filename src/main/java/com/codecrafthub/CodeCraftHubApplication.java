package com.codecrafthub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the CodeCraftHub API.
 *
 * "@SpringBootApplication" tells Spring Boot to:
 *   - scan this package (and sub-packages) for components like
 *     @RestController, @Service, etc. and wire them together
 *   - auto-configure things like the embedded web server and Jackson
 *     JSON support, based on the dependencies in pom.xml
 *
 * Run it with: mvn spring-boot:run
 * Then call:   http://localhost:8080/api/courses
 */
@SpringBootApplication
public class CodeCraftHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeCraftHubApplication.class, args);
    }
}
