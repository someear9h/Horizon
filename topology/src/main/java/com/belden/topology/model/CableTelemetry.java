package com.belden.topology.model;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "cable_telemetry")
public class CableTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cableId;
    private double attenuation;
    private double temperature;
    private double load;
    private double health;
    private LocalDateTime timestamp;
    private LocalDateTime lastSeen;
    private double rulInDays;
}
