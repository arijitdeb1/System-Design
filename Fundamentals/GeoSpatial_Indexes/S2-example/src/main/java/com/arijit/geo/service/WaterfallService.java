package com.arijit.geo.service;


import com.arijit.geo.model.Waterfall;
import com.arijit.geo.repository.WaterfallRepository;
import com.google.common.geometry.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WaterfallService {
    private final WaterfallRepository repository;
    private final GeoService geoService;

    private static final double EARTH_RADIUS_KM = 6371.01;
    private static final int S2_LEVEL = 12; // Match the level used for storing waterfalls

    public Waterfall saveWaterfall(String name, double latitude, double longitude) {
        Waterfall waterfall = new Waterfall();
        waterfall.setName(name);
        waterfall.setLatitude(latitude);
        waterfall.setLongitude(longitude);
        waterfall.setS2CellId(geoService.getS2CellId(latitude, longitude, 12)); // Level 12 for ~3km precision
        return repository.save(waterfall);
    }

    public List<Waterfall> findByS2CellIdInRange(double lat, double lon, long start, long end){
        //return repository.findByS2CellIdInRange(start, end);
        List<Waterfall> waterfalls = repository.findByS2CellIdInRange(start, end);

        // For each waterfall, fetch and add the elevation data
        for (Waterfall waterfall : waterfalls) {
            double latitude = waterfall.getLatitude();
            double longitude = waterfall.getLongitude();
            S2LatLng latLng = S2LatLng.fromDegrees(latitude, longitude);
            S1Angle distance = latLng.getDistance(S2LatLng.fromDegrees(lat, lon));
            double km = distance.radians() * 6371;
            waterfall.setDistance(km);
            System.out.println("------KMs-----"+waterfall.getDistance());
        }

        return waterfalls;

    }


    public List<Waterfall> findWaterfallsWithin10Km(double latitude, double longitude) {
        return findWaterfallsWithinRadius(latitude, longitude, 10.0);
    }

    public List<Waterfall> findWaterfallsWithinRadius(double latitude, double longitude, double radiusKm) {
        // Convert the center point to S2LatLng
        /** This converts the input latitude and longitude (in degrees) to an S2LatLng object,
         which represents a point on the Earth's surface using the S2 geometry system. **/
        S2LatLng center = S2LatLng.fromDegrees(latitude, longitude);

        // Calculate the angle in radians that corresponds to the radius
        /* To work with spherical geometry, we need the radius in radians rather than kilometers
        using below formula */
        double radiusRadians = radiusKm / EARTH_RADIUS_KM;

        // Create an S2Cap representing the circular region
        /* An S2Cap is a portion of the sphere bounded by a circle. This creates a circular region on Earth's surface with:

                The center point we defined
                The radius we calculated in radians */
        S2Cap cap = S2Cap.fromAxisAngle(center.toPoint(), S1Angle.radians(radiusRadians));

        // Create a region coverer
        /* The S2RegionCoverer converts regions (like our circular area) into collections of S2 cells: */
        S2RegionCoverer coverer = S2RegionCoverer.builder()
                .setMaxLevel(S2_LEVEL) /* S2_LEVEL is 12, matching your database storage precision */
                .setMinLevel(S2_LEVEL) /* Allows use of slightly larger cells (level 10) for better coverage */
                .setMaxCells(100) /* Limits the maximum number of cells to prevent performance issues */
                .build();

        // Get the covering S2 cells
        /* This returns a list of S2 cells that completely cover our circular region.
        These cells together form an approximation of our circle. */
        //List<S2CellId> covering = coverer.getCovering(cap).cellIds(); //Use getCovering() when you want efficient coverage with fewer cells, even if they vary in levels.
        ArrayList<S2CellId> covering = new ArrayList<>();
        /* Use getSimpleCovering() when you need strict control over the level of all cells, such as for consistent database indexing or spatial queries that rely on a fixed level.*/
        coverer.getSimpleCovering(cap, center.toPoint(),S2_LEVEL,covering);

        // Convert S2CellId objects to long values for the database query
        List<Long> cellIds = covering.stream()
                .map(S2CellId::id)
                .collect(Collectors.toList());

        // Fetch waterfalls from database that are in these cells
        /* Queries the database for all waterfalls whose S2 cell IDs match the covering cells.
        This is where the S2 spatial indexing provides its biggest benefit -
        we only query for waterfalls in potentially relevant cells.  */
        List<Waterfall> candidateWaterfalls = repository.findByS2CellIds(cellIds);

        // Filter waterfalls to those actually within the exact radius
        // and calculate distances
        List<Waterfall> waterfallsWithinRadius = new ArrayList<>();
        for (Waterfall waterfall : candidateWaterfalls) {
            S2LatLng waterfallLatLng = S2LatLng.fromDegrees(
                    waterfall.getLatitude(), waterfall.getLongitude());

            double distanceKm = EARTH_RADIUS_KM * center.getDistance(waterfallLatLng).radians();

            if (distanceKm <= radiusKm) {
                waterfall.setDistance(distanceKm); // Set the calculated distance
                waterfallsWithinRadius.add(waterfall);
            }
        }

        // Sort by distance (closest first)
        waterfallsWithinRadius.sort((w1, w2) ->
                Double.compare(w1.getDistance(), w2.getDistance()));

        return waterfallsWithinRadius;
    }

    /**
     * Creates a new waterfall and automatically generates the S2 cell ID
     */
    @Transactional
    public Waterfall createWaterfall(String name, double latitude, double longitude) {
        // Validate coordinates
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees");
        }

        // Generate S2 cell ID at level 12
        S2LatLng latLng = S2LatLng.fromDegrees(latitude, longitude);
        S2CellId cellId = S2CellId.fromLatLng(latLng).parent(S2_LEVEL);
        long s2CellId = cellId.id();

        // Create and save the waterfall entity
        Waterfall waterfall = new Waterfall();
        waterfall.setName(name);
        waterfall.setLatitude(latitude);
        waterfall.setLongitude(longitude);
        waterfall.setS2CellId(s2CellId);

        return repository.save(waterfall);
    }

    public double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth's radius in km

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in km
    }
}