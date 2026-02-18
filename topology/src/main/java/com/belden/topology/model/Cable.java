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

    @Id @GeneratedValue
    private Long internalId; // This stops the warning

    @Property("id")
    private Long cableId; // Use this for your business logic

    private String name;
    private String healthStatus;
    private double attenuation;

}
