package com.wineshop.unit.service;

import com.wineshop.exception.WineNotFoundException;
import com.wineshop.model.Wine;
import com.wineshop.repository.WineRepository;
import com.wineshop.service.WineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WineServiceTest {

    private WineService wineService;
    private WineRepository wineRepository;

    @BeforeEach
    void setUp(){
        // Mock the repository
        wineRepository = Mockito.mock(WineRepository.class);

        // Inject the mocked repository into the service
        wineService = new WineService(wineRepository);
    }

    // Tests if the filtering method returns the correct wines based on criteria
    @Test
    void testShouldFilterWinesByCriteria(){
        Wine wine1 = new Wine("Wine A", BigDecimal.valueOf(25), "image1.jpg", null, null, null, null);
        Wine wine2 = new Wine("Wine B", BigDecimal.valueOf(40), "image2.jpg", null, null, null, null);

        // Given a mocked repository returning filtered wines
        when(wineRepository.findAll(any(Specification.class))).thenReturn(List.of(wine1, wine2));

        // When filtering wines by specific criteria
        List<Wine> wines = wineService.filterWines("Red", "Dry", null, "20-50");

        // Then the correct number of wines should be returned
        assertThat(wines).hasSize(2);
        assertThat(wines.stream().map(Wine::getName))
                .containsExactlyInAnyOrder("Wine A", "Wine B");

        // Verify repository method was called with a proper specification
        verify(wineRepository, times(1)).findAll(any(Specification.class));
    }

    // Tests if a wine is correctly retrieved by its ID when it exists
    @Test
    void testShouldReturnWineWhenIdExists(){
        Wine wine1 = new Wine("Wine A", BigDecimal.valueOf(25), "image1.jpg", null, null, null, null);

        // Given a repository containing a wine with ID 1
        when(wineRepository.findById(anyInt())).thenReturn(Optional.of(wine1));

        // When finding wine by ID
        Wine result = wineService.findWineByIdOrThrow(1);

        // Then the correct wine should be returned
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Wine A");

        // Verify repository method was called once
        verify(wineRepository, times(1)).findById(anyInt());
    }


    // Tests if an exception is thrown when trying to find a non-existent wine
    @Test
    void testShouldThrowExceptionWhenIdDoesNotExist(){
        // Given a repository with no matching wine for ID 999
        when(wineRepository.findById(999)).thenReturn(Optional.empty());

        // When attempting to find a non-existent wine, an exception should be thrown
        assertThatThrownBy(() -> wineService.findWineByIdOrThrow(999))
                .isInstanceOf(WineNotFoundException.class)
                .hasMessageContaining("Wine with ID 999 not found");

        // Verify repository method was called once
        verify(wineRepository, times(1)).findById(999);
    }
}
