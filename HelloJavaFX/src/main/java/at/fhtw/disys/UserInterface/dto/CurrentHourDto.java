package at.fhtw.disys.UserInterface.dto;

import java.time.LocalDateTime;

public record CurrentHourDto(
        LocalDateTime hour,
        int id,
        double communityDepleted,
        double gridPortion
) {}
