package com.belden.topology.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImpactAnalysisService {

    private final Neo4jClient neo4jClient;

    public String simulateFailure(Long cableId) {
        // We use .all() to get all impacted lines and join them into a single String
        java.util.List<String> results = neo4jClient.query("""
            MATCH (c:Cable {id:$cableId})-[:CONNECTS_TO]->(s:Switch)
                  -[:FEEDS]->(m:Machine)
                  -[:PART_OF]->(a:AssemblyLine)
            RETURN DISTINCT a.name AS assemblyLine
        """)
                .bind(cableId).to("cableId")
                .fetchAs(String.class)
                .all() // Changed from .one() to .all()
                .stream()
                .toList();

        return results.isEmpty() ? "No impact detected" : String.join(", ", results);
    }
}