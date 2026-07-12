package com.kobukuro.ticketbooking.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VenueControllerIT extends AbstractIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createVenue_returnsCreatedStatus() throws Exception {
        mockMvc.perform(post("/api/v1/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Taipei Arena",
                                  "country": "Taiwan",
                                  "city": "Taipei",
                                  "address": "No. 2, Nanjing E. Rd."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void getVenue_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/venues/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteVenue_thenGetReturns404() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Delete Me Venue",
                                  "country": "Taiwan",
                                  "city": "Taichung",
                                  "address": "Test Address"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String id = com.jayway.jsonpath.JsonPath.read(
                result.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(delete("/api/v1/venues/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/venues/" + id))
                .andExpect(status().isNotFound());
    }
}
