package com.belden.topology.model;


import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

@Node("AssemblyLine")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssemblyLine {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
}