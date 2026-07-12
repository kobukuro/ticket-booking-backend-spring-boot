package com.kobukuro.ticketbooking.event.controller;

import com.kobukuro.ticketbooking.event.dto.CreateVenueRequest;
import com.kobukuro.ticketbooking.event.dto.PagedResponse;
import com.kobukuro.ticketbooking.event.dto.UpdateVenueRequest;
import com.kobukuro.ticketbooking.event.dto.VenueDto;
import com.kobukuro.ticketbooking.event.service.VenueService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @PostMapping
    public ResponseEntity<VenueDto> createVenue(@RequestBody CreateVenueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.create(request));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<VenueDto>> listVenues(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(venueService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueDto> getVenue(@PathVariable UUID id) {
        return ResponseEntity.ok(venueService.findById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<VenueDto> updateVenue(@PathVariable UUID id,
                                                @RequestBody UpdateVenueRequest request) {
        return ResponseEntity.ok(venueService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVenue(@PathVariable UUID id) {
        venueService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
