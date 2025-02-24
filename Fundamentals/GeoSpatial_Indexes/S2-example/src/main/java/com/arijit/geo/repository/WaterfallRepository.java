package com.arijit.geo.repository;

import com.arijit.geo.model.Waterfall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaterfallRepository extends JpaRepository<Waterfall, Long> {
    //List<Waterfall> findByS2CellIdStartingWith(long l);

    @Query("SELECT w FROM Waterfall w WHERE w.s2CellId BETWEEN :start AND :end")
    List<Waterfall> findByS2CellIdInRange(@Param("start") long start, @Param("end") long end);


    // Haversine formula in JPQL to calculate the distance dynamically.
    @Query("""
    SELECT w, 
        (6371 * acos(cos(radians(:lat)) * cos(radians(w.latitude)) * 
        cos(radians(w.longitude) - radians(:lon)) + 
        sin(radians(:lat)) * sin(radians(w.latitude)))) AS distance
    FROM Waterfall w
    WHERE w.s2CellId BETWEEN :start AND :end
    AND (6371 * acos(cos(radians(:lat)) * cos(radians(w.latitude)) * 
        cos(radians(w.longitude) - radians(:lon)) + 
        sin(radians(:lat)) * sin(radians(w.latitude)))) < :radius
    ORDER BY distance
""")
    List<Object[]> findNearbyWithDistance(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("start") long start,
            @Param("end") long end,
            @Param("radius") double radius
    );


}
