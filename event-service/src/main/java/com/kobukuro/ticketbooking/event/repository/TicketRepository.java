package com.kobukuro.ticketbooking.event.repository;

import com.kobukuro.ticketbooking.event.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
}
