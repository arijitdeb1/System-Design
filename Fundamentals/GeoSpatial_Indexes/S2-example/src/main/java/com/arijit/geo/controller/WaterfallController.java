package com.arijit.geo.controller;

import com.arijit.geo.model.Waterfall;
import com.arijit.geo.repository.WaterfallRepository;
import com.arijit.geo.service.GeoService;
import com.arijit.geo.service.WaterfallService;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/waterfalls")
public class WaterfallController {
    private final WaterfallService service;
    private final GeoService geoService;
    private final WaterfallRepository repository;

    /*@PostMapping
    public Waterfall addWaterfall(@RequestParam String name, @RequestParam double lat, @RequestParam double lon) {
        return service.saveWaterfall(name, lat, lon);
    }*/

    /*@GetMapping("/nearby")
    public List<Waterfall> findNearby(@RequestParam double lat, @RequestParam double lon) {
        long s2CellId = geoService.getS2CellId(lat, lon, 12); // Search within level 12 (~3km)
        long prefix = s2CellId / 1000;
        long start = prefix * 1000;
        long end = start + 999; // Cover all possible matches
        //return repository.findByS2CellIdStartingWith(String.valueOf(s2CellId / 1000)); // Match cell prefix
        //return  repository.findByS2CellIdInRange(start, end);
        //return service.findByS2CellIdInRange(lat, lon, start, end);
        return repository.findByS2CellId(s2CellId);
    }*/

 /*   @GetMapping("/nearby/distance")
    public List<Map<String, Serializable>> findNearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "10") double radius) { // Default radius 10km

        long s2CellId = geoService.getS2CellId(lat, lon, 12);
        long prefix = s2CellId / 1000;
        long start = prefix * 1000;
        long end = start + 999;

        List<Object[]> results = repository.findNearbyWithDistance(lat, lon, start, end, radius);

        return results.stream().map(row -> {
            Waterfall waterfall = (Waterfall) row[0];
            double distance = (double) row[1];

            Map<String, Serializable> map = new HashMap<>();
            map.put("id", waterfall.getId());
            map.put("name", waterfall.getName());
            map.put("latitude", waterfall.getLatitude());
            map.put("longitude", waterfall.getLongitude());
            map.put("distance_km", distance);

            return map;
        }).toList();
    }*/

    @GetMapping("/evaluate")
    public void evaluateWaterfall(@RequestParam String name, @RequestParam double lat, @RequestParam double lon) {
        // Convert Latitude/Longitude to S2 Cell ID
        S2LatLng latLng = S2LatLng.fromDegrees(lat, lon);
        S2CellId cellId = S2CellId.fromLatLng(latLng).parent(12); // Level 12

        // Print S2 Cell ID
        System.out.println("S2 Cell ID: " + cellId.id());
        System.out.println("Face: " + cellId.face());
        System.out.println("Level: " + cellId.level());
        System.out.println("Hilbert Index: " + cellId.pos()); // 57-bit Hilbert index
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<Waterfall>> getNearbyWaterfalls(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(required = false, defaultValue = "10.0") double radiusKm) {

        List<Waterfall> nearbyWaterfalls = service.findWaterfallsWithinRadius(
                latitude, longitude, radiusKm);

        return ResponseEntity.ok(nearbyWaterfalls);
    }

    // New endpoint to create a waterfall
    @PostMapping
    public ResponseEntity<Waterfall> createWaterfall(@RequestParam String name, @RequestParam double lat, @RequestParam double lon) {
        Waterfall savedWaterfall = service.createWaterfall(
                name,
                lat,
                lon
        );
        return new ResponseEntity<>(savedWaterfall, HttpStatus.CREATED);
    }

    @GetMapping("/haversine")
    public double getHaversineDistance(@RequestParam double lat1,
                                       @RequestParam double lon1,
                                       @RequestParam double lat2,
                                       @RequestParam double lon2) {
        return service.haversineDistance(lat1, lon1, lat2, lon2);
    }
}