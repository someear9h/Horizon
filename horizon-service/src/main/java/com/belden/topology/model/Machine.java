package com.belden.topology.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

@Node("Machine")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Machine {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
}