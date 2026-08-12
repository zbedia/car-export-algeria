package com.carexport.controller;

import com.carexport.dto.SearchRequest;
import com.carexport.dto.VehicleSearchResult;
import com.carexport.exception.GlobalExceptionHandler;
import com.carexport.service.VehicleSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleController.class)
@Import(GlobalExceptionHandler.class)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleSearchService searchService;

    @Test
    void search_returnsResults_whenVehiclesFound() throws Exception {
        VehicleSearchResult result = new VehicleSearchResult();
        result.setId(1L);
        result.setSource("GarageX");
        result.setBrand("Peugeot");
        result.setModel("308");
        result.setYear(2024);
        result.setPrice(new BigDecimal("18500.00"));
        result.setCurrency("EUR");
        result.setBestPrice(true);

        when(searchService.search(any(SearchRequest.class)))
            .thenReturn(List.of(result));

        mockMvc.perform(get("/api/vehicles/search")
                .param("brand", "Peugeot")
                .param("model", "308")
                .param("maxPrice", "20000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].brand").value("Peugeot"))
            .andExpect(jsonPath("$[0].bestPrice").value(true));
    }

    @Test
    void search_returns200WithEmptyList_whenNoVehicleFound() throws Exception {
        when(searchService.search(any(SearchRequest.class)))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/vehicles/search")
                .param("brand", "Bugatti")
                .param("model", "Chiron"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void search_returns400_whenMaxPriceIsInvalid() throws Exception {
        mockMvc.perform(get("/api/vehicles/search")
                .param("brand", "Peugeot")
                .param("maxPrice", "not-a-number"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void search_returns500_onUnexpectedError() throws Exception {
        when(searchService.search(any(SearchRequest.class)))
            .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/vehicles/search")
                .param("brand", "Renault"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }
}
