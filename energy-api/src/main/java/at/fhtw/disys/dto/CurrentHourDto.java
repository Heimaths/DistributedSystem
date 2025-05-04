// src/main/java/at/fhtw/disys/api/dto/CurrentHourDto.java
package at.fhtw.disys.dto;

import java.time.LocalDateTime;

public record CurrentHourDto(
        LocalDateTime hour,
        double communityProduced,
        double communityUsed,
        double gridUsed,
        double communityPoolPercentage,
        double gridPortionPercentage
) {}
