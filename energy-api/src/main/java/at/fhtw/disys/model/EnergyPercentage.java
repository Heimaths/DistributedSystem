package at.fhtw.disys.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "energy_percentage")
@Data // @Data generiert automatisch Getter & Setter und andere Methoden (z.B. toString()) -> wie in EnergyUsage.java
public class EnergyPercentage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime hour;

    private double communityDepleted;

    private double gridPortion;

}
