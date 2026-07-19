package com.kobukuro.ticketbooking.event.service;

import com.kobukuro.ticketbooking.event.dto.CreateTicketRequest;
import com.kobukuro.ticketbooking.event.dto.PagedResponse;
import com.kobukuro.ticketbooking.event.dto.TicketDto;
import com.kobukuro.ticketbooking.event.dto.UpdateTicketRequest;
import com.kobukuro.ticketbooking.event.entity.Event;
import com.kobukuro.ticketbooking.event.entity.EventStatus;
import com.kobukuro.ticketbooking.event.entity.Ticket;
import com.kobukuro.ticketbooking.event.entity.TicketStatus;
import com.kobukuro.ticketbooking.event.mapper.TicketMapper;
import com.kobukuro.ticketbooking.event.repository.EventRepository;
import com.kobukuro.ticketbooking.event.repository.TicketRepository;
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
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final TicketMapper ticketMapper;

    private Ticket findTicketOrThrow(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found: " + id));
    }

    private Event findEventOrThrow(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: " + id));
    }

    private void requireEventInDraftStatus(Event event) {
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ticket modifications (create, update, delete) are only allowed when the parent event is in DRAFT status. Current event status: " + event.getStatus());
        }
    }

    @Transactional
    public TicketDto createForEvent(UUID eventId, CreateTicketRequest request) {
        Event event = findEventOrThrow(eventId);
        requireEventInDraftStatus(event);
        Ticket ticket = Ticket.builder()
                .event(event)
                .seatNumber(request.seatNumber())
                .price(request.price())
                .status(TicketStatus.AVAILABLE)
                .build();
        return ticketMapper.toDto(ticketRepository.save(ticket));
    }

    public PagedResponse<TicketDto> findByEventId(UUID eventId, Pageable pageable) {
        findEventOrThrow(eventId);
        return PagedResponse.of(ticketRepository.findByEventId(eventId, pageable).map(ticketMapper::toDto));
    }

    public TicketDto findById(UUID id) {
        return ticketMapper.toDto(findTicketOrThrow(id));
    }

    @Transactional
    public TicketDto update(UUID id, UpdateTicketRequest request) {
        Ticket ticket = findTicketOrThrow(id);
        requireEventInDraftStatus(ticket.getEvent());
        ticketMapper.updateTicketFromRequest(request, ticket);
        return ticketMapper.toDto(ticketRepository.save(ticket));
    }

    @Transactional
    public void delete(UUID id) {
        Ticket ticket = findTicketOrThrow(id);
        requireEventInDraftStatus(ticket.getEvent());
        ticketRepository.delete(ticket);
    }
}
