package com.kobukuro.ticketbooking.event.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record VenueDto(
        UUID id,
        String name,
        String country,
        String city,
        String address,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
