package com.example.energy.percentage.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity  // JPA-Entity: wird von Hibernate als Tabelle in der DB verwaltet
@Table(name = "energy_percentage")
@Data   // Lombok: erzeugt automatisch Getter, Setter, ...
public class EnergyPercentage {
    @Id // Primärschlüssel
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hour", nullable = false)
    private LocalDateTime hour;

    @Column(name = "community_depleted")
    private double communityDepleted;

    @Column(name = "grid_portion")
    private double gridPortion;
} 