package com.kobukuro.ticketbooking.event;

import com.kobukuro.ticketbooking.event.dto.EventDto;
import com.kobukuro.ticketbooking.event.dto.TicketDto;
import com.kobukuro.ticketbooking.event.entity.Event;
import com.kobukuro.ticketbooking.event.entity.EventCategory;
import com.kobukuro.ticketbooking.event.entity.EventStatus;
import com.kobukuro.ticketbooking.event.entity.Performer;
import com.kobukuro.ticketbooking.event.entity.Ticket;
import com.kobukuro.ticketbooking.event.entity.TicketStatus;
import com.kobukuro.ticketbooking.event.entity.Venue;
import com.kobukuro.ticketbooking.event.mapper.EventMapper;
import com.kobukuro.ticketbooking.event.mapper.TicketMapper;
import com.kobukuro.ticketbooking.event.repository.EventRepository;
import com.kobukuro.ticketbooking.event.repository.PerformerRepository;
import com.kobukuro.ticketbooking.event.repository.TicketRepository;
import com.kobukuro.ticketbooking.event.repository.VenueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EventDataLayerIT extends AbstractIT {


    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private PerformerRepository performerRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private TicketMapper ticketMapper;

    @Test
    void contextLoadsAndFlywayMigrates() {
        // If we get here, Flyway applied all migrations and ddl-auto=validate passed
    }

    @Test
    void saveEventGeneratesUuidAndTimestamps() {
        Venue venue = venueRepository.save(Venue.builder()
                .name("Taipei Arena")
                .country("Taiwan")
                .city("Taipei")
                .address("No. 2, Nanjing E. Rd.")
                .build());

        Event event = eventRepository.save(Event.builder()
                .name("Spring Concert 2026")
                .description("A great concert")
                .category(EventCategory.CONCERT)
                .venue(venue)
                .eventDateTime(LocalDateTime.of(2026, 12, 25, 19, 0))
                .status(EventStatus.UPCOMING)
                .build());

        assertThat(event.getId()).isNotNull();
        assertThat(event.getCreatedAt()).isNotNull();
        assertThat(event.getUpdatedAt()).isNotNull();
    }

    @Test
    void eventMapperProducesCorrectDto() {
        Venue venue = venueRepository.save(Venue.builder()
                .name("Kaohsiung Arena")
                .country("Taiwan")
                .city("Kaohsiung")
                .address("No. 100, Boai Rd.")
                .build());

        Performer performer = performerRepository.save(Performer.builder()
                .name("Jay Chou")
                .build());

        Event event = Event.builder()
                .name("Jay Chou Concert")
                .category(EventCategory.CONCERT)
                .venue(venue)
                .eventDateTime(LocalDateTime.of(2026, 8, 15, 20, 0))
                .status(EventStatus.UPCOMING)
                .build();
        event.getPerformers().add(performer);
        event = eventRepository.save(event);

        EventDto dto = eventMapper.toDto(event);

        assertThat(dto.id()).isEqualTo(event.getId());
        assertThat(dto.name()).isEqualTo("Jay Chou Concert");
        assertThat(dto.category()).isEqualTo(EventCategory.CONCERT);
        assertThat(dto.status()).isEqualTo(EventStatus.UPCOMING);
        assertThat(dto.venue().name()).isEqualTo("Kaohsiung Arena");
        assertThat(dto.performers()).hasSize(1);
    }

    @Test
    void enumsRoundTripAsStrings() {
        Venue venue = venueRepository.save(Venue.builder()
                .name("Test Venue")
                .country("Taiwan")
                .city("Taichung")
                .address("Test Address")
                .build());

        Event event = eventRepository.save(Event.builder()
                .name("Sports Event")
                .category(EventCategory.SPORTS)
                .venue(venue)
                .eventDateTime(LocalDateTime.of(2026, 10, 1, 14, 0))
                .status(EventStatus.UPCOMING)
                .build());

        Event reloaded = eventRepository.findById(event.getId()).orElseThrow();

        assertThat(reloaded.getCategory()).isEqualTo(EventCategory.SPORTS);
        assertThat(reloaded.getStatus()).isEqualTo(EventStatus.UPCOMING);
    }

    @Test
    void ticketSaveAndMapperWork() {
        Venue venue = venueRepository.save(Venue.builder()
                .name("Ticket Venue")
                .country("Taiwan")
                .city("Taipei")
                .address("Ticket Address")
                .build());

        Event event = eventRepository.save(Event.builder()
                .name("Ticket Test Event")
                .category(EventCategory.CONCERT)
                .venue(venue)
                .eventDateTime(LocalDateTime.of(2026, 11, 1, 19, 0))
                .status(EventStatus.UPCOMING)
                .build());

        Ticket ticket = ticketRepository.save(Ticket.builder()
                .event(event)
                .seatNumber("A1")
                .price(new BigDecimal("1500.00"))
                .status(TicketStatus.AVAILABLE)
                .build());

        assertThat(ticket.getId()).isNotNull();
        assertThat(ticket.getCreatedAt()).isNotNull();

        TicketDto dto = ticketMapper.toDto(ticket);
        assertThat(dto.eventId()).isEqualTo(event.getId());
        assertThat(dto.seatNumber()).isEqualTo("A1");
        assertThat(dto.price()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(dto.status()).isEqualTo(TicketStatus.AVAILABLE);
    }
}
