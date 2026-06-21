package com.kobukuro.ticketbooking.event.repository;

import com.kobukuro.ticketbooking.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
}
