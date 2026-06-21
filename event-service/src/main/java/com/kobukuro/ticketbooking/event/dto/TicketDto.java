package com.kobukuro.ticketbooking.event.dto;

import com.kobukuro.ticketbooking.event.entity.TicketStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TicketDto(
        UUID id,
        UUID eventId,
        String seatNumber,
        BigDecimal price,
        TicketStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
