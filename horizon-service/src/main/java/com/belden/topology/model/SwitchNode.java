package com.belden.topology.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

@Node("Switch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwitchNode {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
}

