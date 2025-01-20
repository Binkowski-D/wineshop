package com.wineshop.integration.controller;

import com.wineshop.model.Wine;
import com.wineshop.repository.WineRepository;
import com.wineshop.util.BaseTestSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WineControllerIntegrationTest extends BaseTestSetup {

    @Autowired
    private MockMvc mockMvc; // MockMvc for simulating HTTP requests

    @Autowired
    private WineRepository wineRepository; // Actual repository for integration

    @BeforeEach
    void setUp(){
        initTestData(); // Initialize test data using BaseTestSetup
    }


    // Tests if the home page is displayed with an empty wine list
    @Test
    void testShouldReturnHomePage() throws Exception{
        mockMvc.perform(get("/"))
                .andExpect(status().isOk()) //Expect HTTP 200
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("wines", "searched"))
                .andExpect(model().attribute("wines", List.of())); // Expect empty wines list initially
    }

    // Tests if filtering wines by color, flavour, and price returns the correct result
    @Test
    void testShouldFilterWinesByCriteria() throws Exception{
        mockMvc.perform(get("/wines")
                .param("color", "Red")
                .param("flavour", "Dry")
                .param("price", "40-50"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("wines", "searched"))
                .andExpect(model().attribute("wines", org.hamcrest.Matchers.hasSize(1))) // 1 wine matches
                .andExpect(model().attribute("wines", org.hamcrest.Matchers.contains(
                        org.hamcrest.Matchers.hasProperty("name", org.hamcrest.Matchers.is("Merlot")))));
    }

    // Tests if wine details are correctly returned when a valid ID is provided
    @Test
    void testShouldReturnWineDetails() throws Exception {
        Wine wine = wineRepository.findAll().get(0);

        mockMvc.perform(get("/wines/" + wine.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("wine-details"))
                .andExpect(model().attributeExists("wine"))
                .andExpect(model().attribute("wine", org.hamcrest.Matchers.hasProperty("name", org.hamcrest.Matchers.is("Cabernet Sauvignon"))));
    }

    // Tests if an error message is displayed when a wine ID does not exist
    @Test
    void testShouldReturnErrorWhenWineNotFound() throws Exception {
        mockMvc.perform(get("/wines/999"))
                .andExpect(status().isOk())
                .andExpect(view().name("wine-details"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", "Wine not found"));
    }

}
