// src/main/java/at/fhtw/disys/api/dto/HistoricDto.java
package at.fhtw.disys.dto;

import java.time.LocalDateTime;

public record HistoricDto(
        LocalDateTime timestamp,
        double communityProduced,
        double communityUsed,
        double gridUsed
) {}
