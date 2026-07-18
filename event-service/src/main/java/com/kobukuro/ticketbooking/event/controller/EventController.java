package com.kobukuro.ticketbooking.event.controller;

import com.kobukuro.ticketbooking.event.dto.CreateEventRequest;
import com.kobukuro.ticketbooking.event.dto.EventDto;
import com.kobukuro.ticketbooking.event.dto.PagedResponse;
import com.kobukuro.ticketbooking.event.dto.UpdateEventRequest;
import com.kobukuro.ticketbooking.event.entity.EventCategory;
import com.kobukuro.ticketbooking.event.entity.EventStatus;
import com.kobukuro.ticketbooking.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping("/api/v1/events")
    public ResponseEntity<EventDto> create(@RequestBody CreateEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(request));
    }

    @GetMapping("/api/v1/events")
    public ResponseEntity<PagedResponse<EventDto>> findAll(
            @RequestParam(required = false) EventCategory category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) LocalDateTime dateFrom,
            @RequestParam(required = false) LocalDateTime dateTo,
            @PageableDefault(size = 20, sort = "eventDateTime", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(eventService.findAll(category, city, status, dateFrom, dateTo, pageable));
    }

    // declared before /{id} to prevent Spring MVC from matching "upcoming" as a UUID path variable
    @GetMapping("/api/v1/events/upcoming")
    public ResponseEntity<PagedResponse<EventDto>> findUpcoming(
            @PageableDefault(size = 20, sort = "eventDateTime", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(eventService.findUpcoming(pageable));
    }

    @GetMapping("/api/v1/events/{id}")
    public ResponseEntity<EventDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.findById(id));
    }

    @PatchMapping("/api/v1/events/{id}")
    public ResponseEntity<EventDto> update(@PathVariable UUID id, @RequestBody UpdateEventRequest request) {
        return ResponseEntity.ok(eventService.update(id, request));
    }

    @DeleteMapping("/api/v1/events/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/events/{id}/publish")
    public ResponseEntity<EventDto> publish(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.publish(id));
    }

    @PostMapping("/api/v1/events/{id}/unpublish")
    public ResponseEntity<EventDto> unpublish(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.unpublish(id));
    }

    @PostMapping("/api/v1/events/{id}/start")
    public ResponseEntity<EventDto> start(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.start(id));
    }

    @PostMapping("/api/v1/events/{id}/cancel")
    public ResponseEntity<EventDto> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.cancel(id));
    }

    @PostMapping("/api/v1/events/{id}/complete")
    public ResponseEntity<EventDto> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.complete(id));
    }

    @PostMapping("/api/v1/events/{id}/performers/{performerId}")
    public ResponseEntity<EventDto> addPerformer(@PathVariable UUID id, @PathVariable UUID performerId) {
        return ResponseEntity.ok(eventService.addPerformer(id, performerId));
    }

    @DeleteMapping("/api/v1/events/{id}/performers/{performerId}")
    public ResponseEntity<EventDto> removePerformer(@PathVariable UUID id, @PathVariable UUID performerId) {
        return ResponseEntity.ok(eventService.removePerformer(id, performerId));
    }
}
