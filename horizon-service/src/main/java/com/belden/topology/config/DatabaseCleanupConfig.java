package com.belden.topology.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseCleanupConfig {

    // use this to clean up the neo4j database at http://localhost:7474/
    // MATCH (n) DETACH DELETE n

    @Bean
    public CommandLineRunner cleanDatabase(JdbcTemplate jdbcTemplate) {
        return args -> {
            System.out.println(">>> [INIT] Wiping old telemetry data for a fresh demo...");
            // TRUNCATE wipes the data but keeps the table structure.
            // RESTART IDENTITY resets the ID counter back to 1.
            try {
                jdbcTemplate.execute("TRUNCATE TABLE cable_telemetry RESTART IDENTITY CASCADE");
                System.out.println(">>> [INIT] Database Cleaned. Ready for baseline.");
            } catch (Exception e) {
                System.out.println(">>> [INIT] Table might not exist yet, skipping cleanup.");
            }
        };
    }
}