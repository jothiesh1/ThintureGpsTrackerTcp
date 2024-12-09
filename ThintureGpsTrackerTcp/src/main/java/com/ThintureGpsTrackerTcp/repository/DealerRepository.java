package com.ThintureGpsTrackerTcp.repository;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ThintureGpsTrackerTcp.model.Dealer;

@Repository
public interface DealerRepository extends JpaRepository<Dealer, Long> {

    Logger logger = LoggerFactory.getLogger(DealerRepository.class);

    @Query("SELECT d FROM Dealer d WHERE d.email = :email")
    Optional<Dealer> findByEmail(@Param("email") String email);
}
