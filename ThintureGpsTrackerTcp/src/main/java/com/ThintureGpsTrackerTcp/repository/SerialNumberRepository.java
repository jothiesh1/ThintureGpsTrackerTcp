package com.ThintureGpsTrackerTcp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ThintureGpsTrackerTcp.model.SerialNumber;

@Repository
public interface SerialNumberRepository extends JpaRepository<SerialNumber, Long> {
    List<SerialNumber> findByDealerId(Long dealerId);
}