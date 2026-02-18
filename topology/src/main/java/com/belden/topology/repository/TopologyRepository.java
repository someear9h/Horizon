package com.belden.topology.repository;

import com.belden.topology.model.Cable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TopologyRepository extends Neo4jRepository<Cable, Long> {

    @Query("""
        MATCH (c:Cable {id:$cableId})-[:CONNECTS_TO]->(s:Switch)
              -[:FEEDS]->(m:Machine)
              -[:PART_OF]->(a:AssemblyLine)
        RETURN a.name
    """)
    String findImpactedAssemblyLine(@Param("cableId") Long cableId);

    @Query("MATCH (c:Cable) WHERE c.id = $cableId " +
            "MATCH (c)-[:CONNECTS_TO]->(s:Switch)-[:FEEDS]->(m:Machine)-[:PART_OF]->(a:AssemblyLine) " +
            "RETURN m.name + '::' + a.name LIMIT 1")
    Optional<String> findImpactDetails(@Param("cableId") Long cableId);
}