package com.kobukuro.ticketbooking.event.repository;

import com.kobukuro.ticketbooking.event.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VenueRepository extends JpaRepository<Venue, UUID> {
}
