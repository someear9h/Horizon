package com.belden.topology.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

@Node("Cable")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cable {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String healthStatus; // HEALTHY / DEGRADED / FAILED
    private double attenuation;

}
