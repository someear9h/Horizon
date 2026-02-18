package com.belden.topology;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class TopologyApplication {

    public static void main(String[] args) {
        SpringApplication.run(TopologyApplication.class, args);
    }

}
