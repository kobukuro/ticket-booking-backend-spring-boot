package com.kobukuro.ticketbooking.event.controller;

import com.kobukuro.ticketbooking.event.dto.CreateTicketRequest;
import com.kobukuro.ticketbooking.event.dto.PagedResponse;
import com.kobukuro.ticketbooking.event.dto.TicketDto;
import com.kobukuro.ticketbooking.event.dto.UpdateTicketRequest;
import com.kobukuro.ticketbooking.event.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/api/v1/events/{eventId}/tickets")
    public ResponseEntity<TicketDto> create(@PathVariable UUID eventId, @RequestBody CreateTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createForEvent(eventId, request));
    }

    @GetMapping("/api/v1/events/{eventId}/tickets")
    public ResponseEntity<PagedResponse<TicketDto>> findByEventId(
            @PathVariable UUID eventId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ticketService.findByEventId(eventId, pageable));
    }

    @GetMapping("/api/v1/tickets/{id}")
    public ResponseEntity<TicketDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ticketService.findById(id));
    }

    @PatchMapping("/api/v1/tickets/{id}")
    public ResponseEntity<TicketDto> update(@PathVariable UUID id, @RequestBody UpdateTicketRequest request) {
        return ResponseEntity.ok(ticketService.update(id, request));
    }

    @DeleteMapping("/api/v1/tickets/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        ticketService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
