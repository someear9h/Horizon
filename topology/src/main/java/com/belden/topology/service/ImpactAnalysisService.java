package com.belden.topology.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImpactAnalysisService {

    private final Neo4jClient neo4jClient;

    public String simulateFailure(Long cableId) {

        return neo4jClient.query("""
            MATCH (c:Cable {id:$cableId})-[:CONNECTS_TO]->(s:Switch)
                  -[:FEEDS]->(m:Machine)
                  -[:PART_OF]->(a:AssemblyLine)
            RETURN a.name AS assemblyLine
        """)
                .bind(cableId).to("cableId")
                .fetchAs(String.class)
                .one()
                .orElse("No impact detected");
    }
}