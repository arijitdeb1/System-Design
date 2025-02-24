package com.arijit.geo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Waterfall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double latitude;
    private double longitude;
    private long s2CellId;
    @Transient
    private double distance;
}
