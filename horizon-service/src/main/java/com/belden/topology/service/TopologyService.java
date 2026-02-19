package com.belden.topology.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TopologyService {

    private final org.springframework.data.neo4j.core.Neo4jClient neo4jClient;

    @Transactional("neo4jTransactionManager")
    public void wipeDatabase() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
    }

    @Transactional("neo4jTransactionManager")
    public void initializeFactoryFloor() {
        // 1. Always wipe the database first to prevent duplicate ghosts
        wipeDatabase();

        // 2. Create the 3 distinct cable networks
        neo4jClient.query("""
            // CREATE CABLE 1 (High Risk - Core Assembly)
            CREATE (c1:Cable {id: 1, name: 'Cable-1 (Main Power)'})
            CREATE (s1:Switch {name: 'Switch-1'})
            CREATE (r1:Machine {name: 'Robot-Arm'})
            CREATE (cb1:Machine {name: 'Conveyor-Belt'})
            CREATE (al1:AssemblyLine {name: 'Assembly-Line-3'})
            CREATE (c1)-[:CONNECTS_TO]->(s1)
            CREATE (s1)-[:FEEDS]->(r1)
            CREATE (s1)-[:FEEDS]->(cb1)
            CREATE (r1)-[:PART_OF]->(al1)
            CREATE (cb1)-[:PART_OF]->(al1)
      
            // CREATE CABLE 2 (Low Risk - Packaging)
            CREATE (c2:Cable {id: 2, name: 'Cable-2 (Data Link)'})
            CREATE (pm:Machine {name: 'Packaging-Unit'})
            CREATE (c2)-[:CONNECTS_TO]->(pm)
        
            // CREATE CABLE 3 (Medium Risk - Cooling)
            CREATE (c3:Cable {id: 3, name: 'Cable-3 (Aux Power)'})
            CREATE (hvac:Machine {name: 'HVAC-Cooling'})
            CREATE (c3)-[:CONNECTS_TO]->(hvac)
        """).run();
    }
}