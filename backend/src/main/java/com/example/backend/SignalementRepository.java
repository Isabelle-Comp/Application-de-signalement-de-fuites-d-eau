package com.example.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface SignalementRepository extends JpaRepository<Signalement, Long> {

    @Query("select s.quartier as quartier, count(s) as total from Signalement s group by s.quartier")
    List<QuartierCount> compterParQuartier();

    interface QuartierCount {
        String getQuartier();
        long getTotal();
    }
}