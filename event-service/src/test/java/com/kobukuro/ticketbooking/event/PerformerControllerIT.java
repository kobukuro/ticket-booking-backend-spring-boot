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

class PerformerControllerIT extends AbstractIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createPerformer_returnsCreatedStatus() throws Exception {
        mockMvc.perform(post("/api/v1/performers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Jay Chou"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void getPerformer_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/performers/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePerformer_thenGetReturns404() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/performers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Delete Me Performer"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String id = com.jayway.jsonpath.JsonPath.read(
                result.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(delete("/api/v1/performers/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/performers/" + id))
                .andExpect(status().isNotFound());
    }
}
