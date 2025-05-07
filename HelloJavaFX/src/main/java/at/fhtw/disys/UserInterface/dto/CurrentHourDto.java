// energy-ui/src/main/java/at/fhtw/disys/UserInterface/dto/CurrentHourDto.java
package at.fhtw.disys.UserInterface.dto;

import java.time.LocalDateTime;

public record CurrentHourDto(
        LocalDateTime hour,
        double communityProduced,
        double communityUsed,
        double gridUsed,
        double communityPoolPercentage,
        double gridPortionPercentage,
        int id,
        double communityDepleted,
        double gridPortion
) {}
