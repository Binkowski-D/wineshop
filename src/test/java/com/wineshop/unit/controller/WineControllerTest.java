package com.wineshop.unit.controller;

import com.wineshop.controller.WineController;
import com.wineshop.exception.WineNotFoundException;
import com.wineshop.model.Wine;
import com.wineshop.service.WineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class WineControllerTest {

    private MockMvc mockMvc;

    private WineService wineService;

    private List<Wine> mockWineList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock the service and set up the controller
        wineService = Mockito.mock(WineService.class);
        WineController wineController = new WineController(wineService);
        mockMvc = MockMvcBuilders.standaloneSetup(wineController).build();

        // Initialize mock data
        mockWineList = new ArrayList<>();
        mockWineList.add(new Wine("Wine A", BigDecimal.valueOf(30), "wine-a.jpeg", null, null, null, null));
        mockWineList.add(new Wine("Wine B", BigDecimal.valueOf(50), "wine-b.jpeg", null, null, null, null));
    }

    // Tests if the home page is displayed correctly with an empty wine list
    @Test
    void testShowHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("wines"))
                .andExpect(model().attribute("wines", new ArrayList<>()));

        verifyNoInteractions(wineService); // Ensure no service calls
    }

    // Tests if filtering wines by criteria returns the expected results
    @Test
    void testFilterWines() throws Exception {
        when(wineService.filterWines("Red", "Dry", null, "20-30")).thenReturn(mockWineList);

        mockMvc.perform(get("/wines")
                        .param("color", "Red")
                        .param("flavour", "Dry")
                        .param("type", (String) null)
                        .param("price", "20-30"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("wines"))
                .andExpect(model().attribute("wines", mockWineList));

        verify(wineService, times(1)).filterWines("Red", "Dry", null, "20-30");
    }

    // Tests if filtering wines with no matching results returns an empty list
    @Test
    void testFilterWinesWithNoResults() throws Exception {
        when(wineService.filterWines("Red", "Sweet", null, "50-60")).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/wines")
                        .param("color", "Red")
                        .param("flavour", "Sweet")
                        .param("type", (String) null)
                        .param("price", "50-60"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("wines"))
                .andExpect(model().attribute("wines", new ArrayList<>()));

        verify(wineService, times(1)).filterWines("Red", "Sweet", null, "50-60");
    }

    // Tests if wine details are displayed correctly for an existing wine ID
    @Test
    void testShowWineDetails() throws Exception {
        Wine mockWine = new Wine("Wine A", BigDecimal.valueOf(30), "wine-a.jpeg", null, null, null, null);
        when(wineService.findWineByIdOrThrow(1)).thenReturn(mockWine);

        mockMvc.perform(get("/wines/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("wine-details"))
                .andExpect(model().attributeExists("wine"))
                .andExpect(model().attribute("wine", mockWine));

        verify(wineService, times(1)).findWineByIdOrThrow(1);
    }

    // Tests if an error message is displayed when trying to fetch a non-existent wine
    @Test
    void testShowWineDetailsNotFound() throws Exception {
        doThrow(new WineNotFoundException("Wine not found"))
                .when(wineService).findWineByIdOrThrow(999);

        mockMvc.perform(get("/wines/999"))
                .andExpect(status().isOk())
                .andExpect(view().name("wine-details"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", "Wine not found"));;


        verify(wineService, times(1)).findWineByIdOrThrow(999);
    }
}