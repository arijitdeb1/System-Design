package com.arijit.geo.repository;

import com.arijit.geo.model.Waterfall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaterfallRepository extends JpaRepository<Waterfall, Long> {
    //List<Waterfall> findByS2CellIdStartingWith(String prefix);

    @Query("SELECT w FROM Waterfall w WHERE w.s2CellId BETWEEN :start AND :end")
    List<Waterfall> findByS2CellIdInRange(@Param("start") long start, @Param("end") long end);

    List<Waterfall> findByS2CellId(long s2cellId);


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


    // Find waterfalls by S2 cell IDs
    @Query("SELECT w FROM Waterfall w WHERE w.s2CellId IN :cellIds")
    List<Waterfall> findByS2CellIds(@Param("cellIds") List<Long> cellIds);
}