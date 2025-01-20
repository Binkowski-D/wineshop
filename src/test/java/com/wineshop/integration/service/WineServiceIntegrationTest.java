package com.wineshop.integration.service;

import com.wineshop.exception.WineNotFoundException;
import com.wineshop.model.Wine;
import com.wineshop.service.WineService;
import com.wineshop.util.BaseTestSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test") // Use the "test" profile for database configuration
public class WineServiceIntegrationTest extends BaseTestSetup {

    @Autowired
    WineService wineService;

    @BeforeEach
    void setUp(){
        initTestData(); // Initialize test data
    }

    // Tests filtering red, dry wines within the price range of 40-50
    @Test
    void testFilterWinesWithAllCriteria(){
        List<Wine> result = wineService.filterWines("Red", "Dry", null, "40-50");

        assertThat(result).hasSize(1); // Expect 1 wine
        assertThat(result.get(0).getName()).isEqualTo("Merlot");
    }

    // Tests if all wines are returned when no filters are applied
    @Test
    void testFilterWinesWithoutCriteria() {
        List<Wine> result = wineService.filterWines(null, null, null, null);

        assertThat(result).hasSize(9); // Expect all wines
    }

    // Tests filtering wines by price range 20-30 without other criteria
    @Test
    void testFilterWinesByPriceOnly() {
        List<Wine> result = wineService.filterWines(null, null, null, "20-30");

        assertThat(result).hasSize(3); // Expect 3 wines
        assertThat(result).extracting(Wine::getName)
                .containsExactlyInAnyOrder("Chardonnay", "Rkatsiteli", "ERA Cabernet");
    }

    // Tests if a wine can be found by its ID
    @Test
    void testFindWineById() {
        Wine wine = wineService.findWineByIdOrThrow(1);

        assertThat(wine).isNotNull();
        assertThat(wine.getName()).isEqualTo("Cabernet Sauvignon");
    }

    // Tests if an exception is thrown when searching for a non-existent wine ID
    @Test
    void testFindWineByIdNotFound() {
        assertThatThrownBy(() -> wineService.findWineByIdOrThrow(999))
                .isInstanceOf(WineNotFoundException.class);
    }
}
