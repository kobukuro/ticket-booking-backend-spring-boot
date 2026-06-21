package com.kobukuro.ticketbooking.event.dto;

import com.kobukuro.ticketbooking.event.entity.EventCategory;
import com.kobukuro.ticketbooking.event.entity.EventStatus;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record EventDto(
        UUID id,
        String name,
        String description,
        EventCategory category,
        VenueDto venue,
        Set<PerformerDto> performers,
        LocalDateTime eventDateTime,
        EventStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
