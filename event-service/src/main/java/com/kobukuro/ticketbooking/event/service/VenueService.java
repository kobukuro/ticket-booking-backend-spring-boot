package com.kobukuro.ticketbooking.event.service;

import com.kobukuro.ticketbooking.event.dto.CreateVenueRequest;
import com.kobukuro.ticketbooking.event.dto.PagedResponse;
import com.kobukuro.ticketbooking.event.dto.UpdateVenueRequest;
import com.kobukuro.ticketbooking.event.dto.VenueDto;
import com.kobukuro.ticketbooking.event.entity.Venue;
import com.kobukuro.ticketbooking.event.mapper.VenueMapper;
import com.kobukuro.ticketbooking.event.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenueService {

    private final VenueRepository venueRepository;
    private final VenueMapper venueMapper;

    @Transactional
    public VenueDto create(CreateVenueRequest request) {
        Venue venue = Venue.builder()
                .name(request.name())
                .country(request.country())
                .city(request.city())
                .address(request.address())
                .build();
        return venueMapper.toDto(venueRepository.save(venue));
    }

    public PagedResponse<VenueDto> findAll(Pageable pageable) {
        return PagedResponse.of(venueRepository.findAll(pageable).map(venueMapper::toDto));
    }

    public VenueDto findById(UUID id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found: " + id));
        return venueMapper.toDto(venue);
    }

    @Transactional
    public VenueDto update(UUID id, UpdateVenueRequest request) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found: " + id));
        venueMapper.updateVenueFromRequest(request, venue);
        return venueMapper.toDto(venueRepository.save(venue));
    }

    @Transactional
    public void delete(UUID id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found: " + id));
        venueRepository.delete(venue);
    }
}
