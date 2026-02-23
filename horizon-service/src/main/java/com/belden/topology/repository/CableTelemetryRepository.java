package com.belden.topology.repository;

import com.belden.topology.model.CableTelemetry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CableTelemetryRepository
        extends JpaRepository<CableTelemetry, Long> {


    Optional<CableTelemetry> findTopByCableIdOrderByTimestampDesc(Long cableId);

    List<CableTelemetry> findByCableIdOrderByTimestampAsc(Long cableId);

    List<CableTelemetry> findByCableIdOrderByTimestampDesc(Long cableId);
}