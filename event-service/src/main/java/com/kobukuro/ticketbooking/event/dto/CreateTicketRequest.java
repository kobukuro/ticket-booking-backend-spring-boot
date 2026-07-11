package com.kobukuro.ticketbooking.event.dto;

import java.math.BigDecimal;

public record CreateTicketRequest(
        String seatNumber,
        BigDecimal price
) {}
