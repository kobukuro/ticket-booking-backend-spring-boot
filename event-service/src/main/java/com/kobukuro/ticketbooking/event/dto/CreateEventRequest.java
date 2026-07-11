package com.kobukuro.ticketbooking.event.dto;

import com.kobukuro.ticketbooking.event.entity.EventCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateEventRequest(
        String name,
        String description,
        EventCategory category,
        UUID venueId,
        List<UUID> performerIds,
        LocalDateTime eventDateTime
) {}
