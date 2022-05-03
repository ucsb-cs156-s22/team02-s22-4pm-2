package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Recommendation;
import edu.ucsb.cs156.example.repositories.RecommendationRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

@WebMvcTest(controllers = RecommendationController.class)
@Import(TestConfig.class)
public class RecommendationControllerTests extends ControllerTestCase {

        @MockBean
        RecommendationRepository recommendationsRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/recommendation/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/recommendation/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/recommendation/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/recommendation?id=1"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/recommendation/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/recommendation/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/recommendation/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime ldt = LocalDateTime.parse("2022-03-11T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2022-04-11T00:00:00");
                Recommendation recommendations = Recommendation.builder()
                                .requesterEmail("kthanigaivelan@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("test")
                                .dateRequested(ldt)
                                .dateNeeded(ldt2)
                                .done(false)
                                .build();

                when(recommendationsRepository.findById(eq(1L))).thenReturn(Optional.of(recommendations));

                // act
                MvcResult response = mockMvc.perform(get("/api/recommendation?id=1"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(recommendationsRepository, times(1)).findById(eq(1L));
                String expectedJson = mapper.writeValueAsString(recommendations);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(recommendationsRepository.findById(eq(1L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/recommendation?id=1"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(recommendationsRepository, times(1)).findById(eq(1L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Recommendation with id 1 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_recommendations() throws Exception {

                // arrange
                LocalDateTime ldt = LocalDateTime.parse("2022-03-11T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2022-04-11T00:00:00");

                Recommendation test1 = Recommendation.builder()
                                .requesterEmail("kthanigaivelan@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("test")
                                .dateRequested(ldt)
                                .dateNeeded(ldt2)
                                .done(false)
                                .build();

                Recommendation test2 = Recommendation.builder()
                                .requesterEmail("kthanigaivelan@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("test2")
                                .dateRequested(ldt2)
                                .dateNeeded(ldt)
                                .done(true)
                                .build();

                ArrayList<Recommendation> expectedRecommendations = new ArrayList<>();
                expectedRecommendations.addAll(Arrays.asList(test1, test2));

                when(recommendationsRepository.findAll()).thenReturn(expectedRecommendations);

                // act
                MvcResult response = mockMvc.perform(get("/api/recommendation/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(recommendationsRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedRecommendations);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_recommendation() throws Exception {
                // arrange
                LocalDateTime ldt = LocalDateTime.parse("2022-03-11T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2022-04-11T00:00:00");
                Recommendation test1 = Recommendation.builder()
                                .requesterEmail("kthanigaivelan@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("test")
                                .dateRequested(ldt)
                                .dateNeeded(ldt2)
                                .done(true)
                                .build();

                when(recommendationsRepository.save(eq(test1))).thenReturn(test1);

                // act
                
                MvcResult response = mockMvc.perform(
                                post("/api/recommendation/post?requesterEmail=kthanigaivelan@ucsb.edu&professorEmail=phtcon@ucsb.edu&explanation=test&dateRequested=2022-03-11T00:00:00&dateNeeded=2022-04-11T00:00:00&done=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recommendationsRepository, times(1)).save(test1);
                String expectedJson = mapper.writeValueAsString(test1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_recommendation() throws Exception {
                // arrange
                LocalDateTime ldt = LocalDateTime.parse("2022-03-11T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2022-04-11T00:00:00");
                Recommendation test1 = Recommendation.builder()
                                .requesterEmail("kthanigaivelan@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("test")
                                .dateRequested(ldt)
                                .dateNeeded(ldt2)
                                .done(false)
                                .build();

                when(recommendationsRepository.findById(eq(1L))).thenReturn(Optional.of(test1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/recommendation?id=1")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recommendationsRepository, times(1)).findById(1L);
                verify(recommendationsRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Recommendation request with id 1 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_recommendation_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(recommendationsRepository.findById(eq(1L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/recommendation?id=1")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(recommendationsRepository, times(1)).findById(1L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Recommendation with id 1 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_recommendation() throws Exception {
                // arrange
                LocalDateTime ldt = LocalDateTime.parse("2022-03-11T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2022-04-11T00:00:00");
                Recommendation test1 = Recommendation.builder()
                                .requesterEmail("kthanigaivelan@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("test")
                                .dateRequested(ldt)
                                .dateNeeded(ldt2)
                                .done(false)
                                .build();

                Recommendation test1Edited = Recommendation.builder()
                                .requesterEmail("kthanigaivelan2@ucsb.edu")
                                .professorEmail("phtcon2@ucsb.edu")
                                .explanation("test2")
                                .dateRequested(ldt2)
                                .dateNeeded(ldt)
                                .done(true)
                                .build();

                String requestBody = mapper.writeValueAsString(test1Edited);

                when(recommendationsRepository.findById(eq(1L))).thenReturn(Optional.of(test1));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/recommendation?id=1")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recommendationsRepository, times(1)).findById(1L);
                verify(recommendationsRepository, times(1)).save(test1Edited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_recommendation_that_does_not_exist() throws Exception {
                // arrange
                LocalDateTime ldt = LocalDateTime.parse("2022-03-11T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2022-04-11T00:00:00");
                Recommendation test1Edited = Recommendation.builder()
                                .requesterEmail("kthanigaivelan@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("test")
                                .dateRequested(ldt)
                                .dateNeeded(ldt2)
                                .done(true)
                                .build();

                String requestBody = mapper.writeValueAsString(test1Edited);

                when(recommendationsRepository.findById(eq(1L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/recommendation?id=1")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(recommendationsRepository, times(1)).findById(1L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Recommendation with id 1 not found", json.get("message"));

        }
}
