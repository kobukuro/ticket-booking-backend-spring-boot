package com.kobukuro.ticketbooking.event.dto;

public record CreateVenueRequest(
        String name,
        String country,
        String city,
        String address
) {}
