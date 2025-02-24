package com.arijit.geo.service;


import com.arijit.geo.model.Waterfall;
import com.arijit.geo.repository.WaterfallRepository;
import com.google.common.geometry.S1Angle;
import com.google.common.geometry.S2LatLng;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WaterfallService {
    private final WaterfallRepository repository;
    private final GeoService geoService;

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
}
