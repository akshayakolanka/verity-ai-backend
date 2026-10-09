package com.verity;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication
public class VerityBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(VerityBackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner testMongoConnection(MongoTemplate mongoTemplate) {
        return args -> {
            // Returns the database name from your Atlas connection string ("verity")
            String dbName = mongoTemplate.getDb().getName();
            System.out.println("==========================================");
            System.out.println("SUCCESSFULLY CONNECTED TO MONGODB DATABASE: " + dbName);
            System.out.println("==========================================");
        };
    }
}