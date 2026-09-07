package com.carexport.controller;

import com.carexport.config.SecurityConfig;
import com.carexport.dto.SourceHealthDto;
import com.carexport.exception.GlobalExceptionHandler;
import com.carexport.scraping.ScrapingScheduler;
import com.carexport.service.ScrapingHealthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScrapingHealthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ScrapingHealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScrapingHealthService healthService;

    @MockBean
    private ScrapingScheduler scrapingScheduler;

    @Test
    void health_returnsUnknown_whenNothingRecordedYet() throws Exception {
        when(healthService.snapshot()).thenReturn(List.of());

        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UNKNOWN"))
            .andExpect(jsonPath("$.sources").isEmpty());
    }

    @Test
    void health_aggregatesSourceStatuses() throws Exception {
        when(healthService.snapshot()).thenReturn(List.of(
            new SourceHealthDto("CarXport", SourceHealthDto.Status.UP, 1, 0, 0, null, null, 24, null),
            new SourceHealthDto("ExportCar213", SourceHealthDto.Status.EMPTY, 0, 0, 0, null, null, 0, null)
        ));

        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("EMPTY"))
            .andExpect(jsonPath("$.sources", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void refresh_runsSchedulerRound_andReturnsFreshSnapshot() throws Exception {
        when(healthService.snapshot()).thenReturn(List.of(
            new SourceHealthDto("CarXport", SourceHealthDto.Status.UP, 1, 0, 0, null, null, 24, null)
        ));

        mockMvc.perform(post("/api/health/refresh"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.sources", hasSize(1)));

        verify(scrapingScheduler).refreshListings();
    }

    @Test
    void refresh_returnsUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/health/refresh"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void refresh_returnsForbidden_whenAuthenticatedWithoutAdminRole() throws Exception {
        mockMvc.perform(post("/api/health/refresh"))
            .andExpect(status().isForbidden());
    }

    @Test
    void refresh_returnsMethodNotAllowed_whenCalledWithGet() throws Exception {
        mockMvc.perform(get("/api/health/refresh"))
            .andExpect(status().isMethodNotAllowed());
    }
}