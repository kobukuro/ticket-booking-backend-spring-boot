package com.kobukuro.ticketbooking.event.service;

import com.kobukuro.ticketbooking.event.dto.CreateEventRequest;
import com.kobukuro.ticketbooking.event.dto.EventDto;
import com.kobukuro.ticketbooking.event.dto.PagedResponse;
import com.kobukuro.ticketbooking.event.dto.UpdateEventRequest;
import com.kobukuro.ticketbooking.event.entity.Event;
import com.kobukuro.ticketbooking.event.entity.EventCategory;
import com.kobukuro.ticketbooking.event.entity.EventStatus;
import com.kobukuro.ticketbooking.event.entity.Performer;
import com.kobukuro.ticketbooking.event.entity.Venue;
import com.kobukuro.ticketbooking.event.mapper.EventMapper;
import com.kobukuro.ticketbooking.event.repository.EventRepository;
import com.kobukuro.ticketbooking.event.repository.PerformerRepository;
import com.kobukuro.ticketbooking.event.repository.VenueRepository;
import com.kobukuro.ticketbooking.event.specification.EventSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final PerformerRepository performerRepository;
    private final EventMapper eventMapper;

    private Event findEventOrThrow(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: " + id));
    }

    @Transactional
    public EventDto create(CreateEventRequest request) {
        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found: " + request.venueId()));
        Event event = Event.builder()
                .name(request.name())
                .description(request.description())
                .category(request.category())
                .venue(venue)
                .eventDateTime(request.eventDateTime())
                .status(EventStatus.DRAFT)
                .build();
        if (request.performerIds() != null && !request.performerIds().isEmpty()) {
            List<Performer> performers = performerRepository.findAllById(request.performerIds());
            event.getPerformers().addAll(performers);
        }
        Event saved = eventRepository.save(event);
        return eventMapper.toDto(saved);
    }

    public PagedResponse<EventDto> findAll(EventCategory category, String city, EventStatus status,
                                           LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
        Specification<Event> spec = Specification.where(EventSpecification.hasCategory(category))
                .and(EventSpecification.hasCity(city))
                .and(EventSpecification.hasStatus(status))
                .and(EventSpecification.eventDateAfter(dateFrom))
                .and(EventSpecification.eventDateBefore(dateTo));
        Page<EventDto> dtoPage = eventRepository.findAll(spec, pageable).map(eventMapper::toDto);
        return PagedResponse.of(dtoPage);
    }

    public EventDto findById(UUID id) {
        return eventMapper.toDto(findEventOrThrow(id));
    }

    public PagedResponse<EventDto> findUpcoming(Pageable pageable) {
        Specification<Event> spec = Specification.where(EventSpecification.hasStatus(EventStatus.UPCOMING))
                .and(EventSpecification.eventDateAfter(LocalDateTime.now()));
        Page<EventDto> dtoPage = eventRepository.findAll(spec, pageable).map(eventMapper::toDto);
        return PagedResponse.of(dtoPage);
    }

    @Transactional
    public EventDto update(UUID id, UpdateEventRequest request) {
        Event event = findEventOrThrow(id);
        if (request.getVenueId() != null) {
            Venue venue = venueRepository.findById(request.getVenueId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found: " + request.getVenueId()));
            event.setVenue(venue);
        }
        eventMapper.updateEventFromRequest(request, event);
        Event saved = eventRepository.save(event);
        return eventMapper.toDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Event event = findEventOrThrow(id);
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Event can only be deleted when in DRAFT status. Current status: " + event.getStatus());
        }
        eventRepository.delete(event);
    }

    @Transactional
    public EventDto publish(UUID id) {
        Event event = findEventOrThrow(id);
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Event must be in DRAFT status to publish. Current status: " + event.getStatus());
        }
        event.setStatus(EventStatus.UPCOMING);
        return eventMapper.toDto(eventRepository.save(event));
    }

    @Transactional
    public EventDto unpublish(UUID id) {
        Event event = findEventOrThrow(id);
        if (event.getStatus() != EventStatus.UPCOMING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Event must be in UPCOMING status to unpublish. Current status: " + event.getStatus());
        }
        event.setStatus(EventStatus.DRAFT);
        return eventMapper.toDto(eventRepository.save(event));
    }

    @Transactional
    public EventDto start(UUID id) {
        Event event = findEventOrThrow(id);
        if (event.getStatus() != EventStatus.UPCOMING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Event must be in UPCOMING status to start. Current status: " + event.getStatus());
        }
        event.setStatus(EventStatus.ONGOING);
        return eventMapper.toDto(eventRepository.save(event));
    }

    @Transactional
    public EventDto cancel(UUID id) {
        Event event = findEventOrThrow(id);
        if (event.getStatus() != EventStatus.UPCOMING && event.getStatus() != EventStatus.ONGOING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Event can only be cancelled from UPCOMING or ONGOING status. Current status: " + event.getStatus());
        }
        event.setStatus(EventStatus.CANCELLED);
        return eventMapper.toDto(eventRepository.save(event));
    }

    @Transactional
    public EventDto complete(UUID id) {
        Event event = findEventOrThrow(id);
        if (event.getStatus() != EventStatus.ONGOING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Event must be in ONGOING status to complete. Current status: " + event.getStatus());
        }
        event.setStatus(EventStatus.COMPLETED);
        return eventMapper.toDto(eventRepository.save(event));
    }

    @Transactional
    public EventDto addPerformer(UUID eventId, UUID performerId) {
        Event event = findEventOrThrow(eventId);
        Performer performer = performerRepository.findById(performerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Performer not found: " + performerId));
        event.getPerformers().add(performer);
        return eventMapper.toDto(eventRepository.save(event));
    }

    @Transactional
    public EventDto removePerformer(UUID eventId, UUID performerId) {
        Event event = findEventOrThrow(eventId);
        Performer performer = performerRepository.findById(performerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Performer not found: " + performerId));
        event.getPerformers().remove(performer);
        return eventMapper.toDto(eventRepository.save(event));
    }
}
