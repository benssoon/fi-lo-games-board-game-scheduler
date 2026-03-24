package nl.benzelinsky.filogames.controllers;

import com.jayway.jsonpath.JsonPath;
import jakarta.transaction.Transactional;
import nl.benzelinsky.filogames.models.Event;
import nl.benzelinsky.filogames.models.User;
import nl.benzelinsky.filogames.repositories.EventRepository;
import nl.benzelinsky.filogames.repositories.GameRepository;
import nl.benzelinsky.filogames.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Turn off security
@ActiveProfiles("test")
class EventControllerIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private GameRepository gameRepository;

    @Test
    @DisplayName("Should create correct Event")
    @WithMockUser("ben")
    void testCreateEvent() throws Exception {

        String requestJson = """
                {
                    "name": "Root night",
                    "isHostPlaying": true,
                    "location": "Rotterdam",
                    "gameId": "1"
                }
                """;

        MvcResult result = this.mockMvc
                .perform(MockMvcRequestBuilders.post("/events")
                        .contentType(APPLICATION_JSON)
                        .content(requestJson)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        Long createdId = id.longValue();

        assertNotNull(createdId);
        assertEquals("Root night", eventRepository.findById(createdId).orElseThrow().getName());
        assertThat(result.getResponse().getHeader("Location"), matchesPattern(".*/events/" + createdId));
    }

    @Test
    @Transactional
    @DisplayName("Should add player with username test_user to event with ID 1")
    void testAddPlayer() throws Exception {
        String username = "test_user";
        Long eventId = 1L;

        MvcResult result = this.mockMvc
                .perform(MockMvcRequestBuilders.post("/events/" + eventId + "/add-player/" + username))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        User testUser = userRepository.findById(username).orElseThrow();

        List<Object> players = JsonPath.read(result.getResponse().getContentAsString(), "$.players");
        assertTrue(players.contains(username));
        assertTrue(eventRepository.findById(eventId).orElseThrow().getPlayers().contains(testUser));
    }
}