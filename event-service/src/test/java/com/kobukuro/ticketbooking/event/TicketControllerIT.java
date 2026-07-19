package com.kobukuro.ticketbooking.event;

import com.kobukuro.ticketbooking.event.entity.Venue;
import com.kobukuro.ticketbooking.event.repository.VenueRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TicketControllerIT extends AbstractIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VenueRepository venueRepository;

    private String createVenueAndGetId() {
        Venue venue = venueRepository.save(Venue.builder()
                .name("Test Venue")
                .country("Taiwan")
                .city("Taipei")
                .address("No. 1, Test Rd.")
                .build());
        return venue.getId().toString();
    }

    private String createEventAndGetId(String venueId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Test Concert",
                                  "category": "CONCERT",
                                  "venue_id": "%s",
                                  "event_date_time": "2027-01-01T20:00:00"
                                }
                                """.formatted(venueId)))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    @Test
    void createTicket_forDraftEvent_returns201() throws Exception {
        String venueId = createVenueAndGetId();
        String eventId = createEventAndGetId(venueId);
        mockMvc.perform(post("/api/v1/events/" + eventId + "/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "seat_number": "A1",
                                  "price": 500.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void createTicket_forNonDraftEvent_returns409() throws Exception {
        String venueId = createVenueAndGetId();
        String eventId = createEventAndGetId(venueId);
        mockMvc.perform(post("/api/v1/events/" + eventId + "/publish"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/events/" + eventId + "/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "seat_number": "B2",
                                  "price": 300.00
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void getTicket_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/tickets/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }
}
