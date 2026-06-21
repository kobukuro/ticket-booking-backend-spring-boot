package com.kobukuro.ticketbooking.event.repository;

import com.kobukuro.ticketbooking.event.entity.Performer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PerformerRepository extends JpaRepository<Performer, UUID> {
}
