package com.kobukuro.ticketbooking.event;

import com.kobukuro.ticketbooking.event.entity.Venue;
import com.kobukuro.ticketbooking.event.repository.VenueRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventControllerIT extends AbstractIT {

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
    void createEvent_returnsCreatedWithDraftStatus() throws Exception {
        String venueId = createVenueAndGetId();
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Rock Festival",
                                  "category": "CONCERT",
                                  "venue_id": "%s",
                                  "event_date_time": "2027-06-15T19:00:00"
                                }
                                """.formatted(venueId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void getEvent_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/events/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listEvents_returnsNonEmptyPage() throws Exception {
        String venueId = createVenueAndGetId();
        createEventAndGetId(venueId);
        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(greaterThan(0)));
    }

    @Test
    void publishEvent_transitionsDraftToUpcoming() throws Exception {
        String venueId = createVenueAndGetId();
        String eventId = createEventAndGetId(venueId);
        mockMvc.perform(post("/api/v1/events/" + eventId + "/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UPCOMING"));
    }

    @Test
    void deleteEvent_draftStatus_returns204() throws Exception {
        String venueId = createVenueAndGetId();
        String eventId = createEventAndGetId(venueId);
        mockMvc.perform(delete("/api/v1/events/" + eventId))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/events/" + eventId))
                .andExpect(status().isNotFound());
    }
}
