package com.arijit.geo.service;

import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class GeoService {

    public long getS2CellId(double latitude, double longitude, int level) {
        S2LatLng latLng = S2LatLng.fromDegrees(latitude, longitude);
        return S2CellId.fromLatLng(latLng).parent(level).id();
    }
}
