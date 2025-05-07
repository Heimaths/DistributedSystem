// src/main/java/at/fhtw/disys/api/dto/HistoricDto.java
package at.fhtw.disys.UserInterface.dto;

import java.time.LocalDateTime;

public record HistoricDto(
        LocalDateTime timestamp,
        LocalDateTime hour,
        double communityProduced,
        double communityUsed,
        double gridUsed,
        int id

       /*double communityPoolPercentage,
        double gridPortionPercentage,
        double communityDepleted,
        double gridPortion
        */
) {}
