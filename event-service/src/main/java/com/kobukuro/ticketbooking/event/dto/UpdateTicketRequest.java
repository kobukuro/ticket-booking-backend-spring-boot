package com.kobukuro.ticketbooking.event.dto;

import com.kobukuro.ticketbooking.event.entity.TicketStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class UpdateTicketRequest {
    private String seatNumber;
    private BigDecimal price;
    private TicketStatus status;
}
