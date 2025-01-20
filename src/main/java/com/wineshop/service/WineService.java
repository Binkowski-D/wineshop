package com.wineshop.service;

import com.wineshop.exception.WineNotFoundException;
import com.wineshop.model.Wine;
import com.wineshop.repository.WineRepository;
import com.wineshop.specification.WineSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WineService {
    private final WineRepository wineRepository;
    private static final Logger logger = LoggerFactory.getLogger(WineService.class);

    public WineService(WineRepository wineRepository){
        this.wineRepository = wineRepository;
    }

    // Finds wines matching given filters
    public List<Wine> filterWines(String color, String flavour, String type, String priceRange){
        logger.info("Filtering wines with criteria - Color: {}, Flavour: {}, Type: {}, Price Range: {}",
                color, flavour, type, priceRange);


        BigDecimal[] priceRangeValues = mapPriceRange(priceRange);

        // Build specification and fetch wines
        Specification<Wine> specification = WineSpecification.filter(color, flavour, type, priceRangeValues[0], priceRangeValues[1]);
        List<Wine> wines = wineRepository.findAll(specification);

        logger.info("Found {} wines matching the criteria", wines.size());
        return wines;
    }

    // Gets a wine by ID or throws an exception if not found
    public Wine findWineByIdOrThrow(Integer id){
        logger.info("Fetching wine by ID: {}", id);
        return wineRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Wine with ID {} not found", id);
                    return new WineNotFoundException("Wine with ID " + id + " not found");
                });
    }

    // Maps price range string to min and max price values
    private BigDecimal[] mapPriceRange(String priceRange) {

        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;

        if (priceRange != null && !priceRange.trim().isEmpty()) {
            switch (priceRange) {
                case "<20" -> maxPrice = BigDecimal.valueOf(20);
                case "20-30" -> {
                    minPrice = BigDecimal.valueOf(20);
                    maxPrice = BigDecimal.valueOf(30);
                }
                case "30-40" -> {
                    minPrice = BigDecimal.valueOf(30);
                    maxPrice = BigDecimal.valueOf(40);
                }
                case "40-50" -> {
                    minPrice = BigDecimal.valueOf(40);
                    maxPrice = BigDecimal.valueOf(50);
                }
                case ">50" -> minPrice = BigDecimal.valueOf(50);
                default -> logger.warn("Unknown price range: {}", priceRange);
            }
        }

        logger.info("Mapped priceRange '{}' to minPrice={}, maxPrice={}", priceRange, minPrice, maxPrice);
        return new BigDecimal[]{minPrice, maxPrice};
    }



}
