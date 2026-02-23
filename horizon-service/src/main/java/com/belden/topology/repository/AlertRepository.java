package com.belden.topology.repository;

import com.belden.topology.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    // Fetches only the active, unread alerts for the frontend notification bell
    List<Alert> findByIsReadFalseOrderByTimestampDesc();

    // Used for the "Lifecycle Audit Report" to show all historical alerts for a cable
    List<Alert> findByCableIdOrderByTimestampDesc(Long cableId);
}