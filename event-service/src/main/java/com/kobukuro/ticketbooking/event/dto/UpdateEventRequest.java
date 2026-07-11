package com.kobukuro.ticketbooking.event.dto;

import com.kobukuro.ticketbooking.event.entity.EventCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UpdateEventRequest {
    private String name;
    private String description;
    private EventCategory category;
    private UUID venueId;
    private LocalDateTime eventDateTime;
}
