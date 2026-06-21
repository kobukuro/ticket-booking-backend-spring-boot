package com.kobukuro.ticketbooking.event.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PerformerDto(
        UUID id,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
