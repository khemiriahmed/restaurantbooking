package com.akhm.restaurantbooking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration du parcours de réservation complet :
 * authentification, consultation des restaurants, création d'une
 * réservation et contrôle d'accès.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationFlowTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void unauthRequestIsRejected() throws Exception {

        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void restaurantArePubliclyReadable() throws Exception {

        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    void fullReservationFlow() throws Exception {

        String token = login("client@restaurant-booking.com", "Client@12345");

        // 1. Un restaurant existe
        MvcResult restaurantResult = mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode restaurants = objectMapper
                .readTree(restaurantResult.getResponse().getContentAsString());

        long restaurantId = restaurants.get(0).get("id").asLong();

        // 2. Ce restaurant propose des tables disponibles
        MvcResult tableResult = mockMvc.perform(
                        get("/api/restaurants/{id}/tables/available", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andReturn();

        JsonNode table = objectMapper
                .readTree(tableResult.getResponse().getContentAsString())
                .get(0);

        long tableId = table.get("id").asLong();

        // 3. Création d'une réservation (un jour non fermé, à une heure d'ouverture)
        LocalDate date = LocalDate.now().plusDays(1);

        while (date.getDayOfWeek() == DayOfWeek.MONDAY) {
            date = date.plusDays(1);
        }

        String body = """
                {
                    "restaurantId": %d,
                    "tableId": %d,
                    "reservationDate": "%s",
                    "startTime": "19:00:00",
                    "endTime": "20:00:00",
                    "numberOfPeople": 2
                }
                """.formatted(restaurantId, tableId, date);

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.restaurantName").exists());

        // 4. Le client retrouve sa réservation
        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));

        // 5. Le restaurateur peut confirmer la réservation
        String ownerToken = login("owner@restaurant-booking.com", "Owner@12345");

        MvcResult ownedResult = mockMvc.perform(get("/api/reservations")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andReturn();

        JsonNode reservations = objectMapper
                .readTree(ownedResult.getResponse().getContentAsString());

        long reservationId = reservations.get(0).get("id").asLong();

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .patch("/api/reservations/{id}/confirm", reservationId)
                                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    private String login(String email, String password) throws Exception {

        String body = """
                { "email": "%s", "password": "%s" }
                """.formatted(email, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper
                .readTree(result.getResponse().getContentAsString());

        return json.get("token").asText();
    }
}