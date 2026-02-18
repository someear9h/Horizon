package com.belden.topology.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TopologyService {


    private final org.springframework.data.neo4j.core.Neo4jClient neo4jClient;

    @Transactional("transactionManager")
    public void createSampleTopology() {

        neo4jClient.query("""
            CREATE (c:Cable {id:1, name:'Cable-1', healthStatus:'HEALTHY', attenuation:0.2})
            CREATE (s:Switch {id:2, name:'Switch-1'})
            CREATE (m:Machine {id:3, name:'Robot-Arm'})
            CREATE (a:AssemblyLine {id:4, name:'Assembly-Line-3'})
            CREATE (c)-[:CONNECTS_TO]->(s)
            CREATE (s)-[:FEEDS]->(m)
            CREATE (m)-[:PART_OF]->(a)
            CREATE (m2:Machine {id:5, name:'Conveyor-Belt'})
            CREATE (s)-[:FEEDS]->(m2)
            CREATE (m2)-[:PART_OF]->(a)
        """).run();
    }

}