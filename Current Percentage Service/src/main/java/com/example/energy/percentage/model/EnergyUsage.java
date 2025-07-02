package com.example.energy.percentage.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity // JPA-Entity: wird von Hibernate als Tabelle in der DB verwaltet
@Table(name = "energy_usage")
@Data // Lombok: erzeugt automatisch Getter, Setter, ...
public class EnergyUsage {
    @Id // Primärschlüssel
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hour", nullable = false)
    private LocalDateTime hour;

    @Column(name = "community_produced")
    private double communityProduced;

    @Column(name = "community_used")
    private double communityUsed;

    @Column(name = "grid_used")
    private double gridUsed;
} 