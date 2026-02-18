package com.belden.topology.config;

import jakarta.persistence.EntityManagerFactory;
import org.neo4j.driver.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    // 1. PRIMARY Transaction Manager for JPA (SQL Database)
    // This fixes the "No bean named 'transactionManager'" error for your TelemetryConsumer
    @Bean(name = "transactionManager")
    @Primary
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    // 2. SECONDARY Transaction Manager for Neo4j (Graph Database)
    // Used explicitly by AlertService and TopologyService via @Transactional("neo4jTransactionManager")
    @Bean(name = "neo4jTransactionManager")
    public PlatformTransactionManager neo4jTransactionManager(Driver driver) {
        return new Neo4jTransactionManager(driver);
    }
}