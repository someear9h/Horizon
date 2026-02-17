package com.belden.topology.repository;

import com.belden.topology.model.Cable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

public interface TopologyRepository extends Neo4jRepository<Cable, Long> {

    @Query("""
        MATCH (c:Cable {id:$cableId})-[:CONNECTS_TO]->(s:Switch)
              -[:FEEDS]->(m:Machine)
              -[:PART_OF]->(a:AssemblyLine)
        RETURN a.name
    """)
    String findImpactedAssemblyLine(@Param("cableId") Long cableId);
}