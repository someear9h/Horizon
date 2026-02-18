package com.belden.topology.repository;

import com.belden.topology.model.CableTelemetry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CableTelemetryRepository
        extends JpaRepository<CableTelemetry, Long> {

    List<CableTelemetry> findTop2ByCableIdOrderByTimestampDesc(Long cableId);
}