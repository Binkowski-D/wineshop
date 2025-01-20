package com.wineshop.integration.specification;

import com.wineshop.model.Wine;
import com.wineshop.specification.WineSpecification;
import com.wineshop.util.BaseTestSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // Configures JPA tests with an in-memory database
@ActiveProfiles("test") // Use the "test" profile for database configuration
@Import(WineSpecification.class) // Import the WineSpecification class for testing
public class WineSpecificationTest extends BaseTestSetup {

    @BeforeEach
    void setUp(){
        initTestData(); // Load test data before each test
    }

    // Tests filtering wines by the color "Red"
    @Test
    void testFilterByColor(){
        Specification<Wine> specification = WineSpecification.hasColor("Red");
        List<Wine> result = wineRepository.findAll(specification);

        assertThat(result).hasSize(4); // Expect 4 wines with color "Red"
        assertThat(result).extracting(Wine::getName)
                .containsExactlyInAnyOrder("Cabernet Sauvignon", "Merlot", "Kindzmarauli", "ERA Cabernet");
    }

    // Tests filtering wines by a non-existing color "Black" (should return empty)
    @Test
    void testFilterByNonExistingColor(){
        Specification<Wine> specification = WineSpecification.hasColor("Black");
        List<Wine> result = wineRepository.findAll(specification);

        assertThat(result).isEmpty(); // Expect no results
    }

    // Tests filtering wines by the flavour "Dry"
    @Test
    void testFilterByFlavour(){
        Specification<Wine> specification = WineSpecification.hasFlavour("Dry");
        List<Wine> result = wineRepository.findAll(specification);

        assertThat(result).hasSize(4); // Expect 4 wines with flavour "Dry"
        assertThat(result).extracting(Wine::getName)
                .containsExactlyInAnyOrder("Cabernet Sauvignon", "Merlot", "Prosecco", "Sauvignon Blanc");
    }

    // Tests filtering wines by the type "Sparkling"
    @Test
    void testFilterByType(){
        Specification<Wine> specification = WineSpecification.hasType("Sparkling");
        List<Wine> result = wineRepository.findAll(specification);

        assertThat(result).hasSize(1); // Expect 1 wine with type "Sparkling"
        assertThat(result.get(0).getName()).isEqualTo("Prosecco");
    }

    // Tests filtering wines within the price range of 20 to 50
    @Test
    void testFilterByPrice(){
        Specification<Wine> specification = WineSpecification.hasPriceBetween(BigDecimal.valueOf(20), BigDecimal.valueOf(50));
        List<Wine> result = wineRepository.findAll(specification);

        assertThat(result).hasSize(5); // Expect 5 wines in the price range
        assertThat(result).extracting(Wine::getName)
                .containsExactlyInAnyOrder("Merlot", "Chardonnay", "Kindzmarauli", "Rkatsiteli", "ERA Cabernet");
    }

    // Tests filtering wines by multiple criteria (color, flavour, and price)
    @Test
    void testCombineFilters(){
        Specification<Wine> specification = WineSpecification.filter("Red", "Dry", null, BigDecimal.valueOf(20), BigDecimal.valueOf(50));
        List<Wine> result = wineRepository.findAll(specification);

        assertThat(result).hasSize(1); // Expect 1 wine matching all filters
        assertThat(result.get(0).getName()).isEqualTo("Merlot");
    }

    // Tests if all wines are returned when no filters are applied
    @Test
    void testReturnAllWinesIfNoFiltersAreApplied(){
        Specification<Wine> specification = WineSpecification.filter(null, null, null, null, null);
        List<Wine> result = wineRepository.findAll(specification);

        assertThat(result).hasSize(9); // Expect all 9 wines
    }

}
