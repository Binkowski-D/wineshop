package com.wineshop.specification;

import com.wineshop.model.Wine;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class WineSpecification {

    // Filters wines by color. Returns all wines if no color is specified.
    public static Specification<Wine> hasColor(String color) {
        return (root, query, criteriaBuilder) -> {
            if (color == null || color.trim().isEmpty() || color.equals(" ")) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("color").get("name"), color);
        };
    }

    // Filters wines by flavour. Returns all wines if no flavour is specified.
    public static Specification<Wine> hasFlavour(String flavour) {
        return (root, query, criteriaBuilder) -> {
            if (flavour == null || flavour.trim().isEmpty() || flavour.equals(" ")) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("flavour").get("name"), flavour);
        };
    }

    // Filters wines by type. Returns all wines if no type is specified.
    public static Specification<Wine> hasType(String type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null || type.trim().isEmpty() || type.equals(" ")) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("type").get("name"), type);
        };
    }

    // Filters wines by price range. Handles cases where min and/or max price is null.
    public static Specification<Wine> hasPriceBetween(BigDecimal minPrice, BigDecimal maxPrice){
        return (root, query, criteriaBuilder) -> {
            if(minPrice == null && maxPrice == null){
                return criteriaBuilder.conjunction();
            }else if(minPrice == null){
                return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
            }else if(maxPrice == null){
                return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
            }else{
                return criteriaBuilder.between(root.get("price"), minPrice, maxPrice);
            }
        };
    }

    // Combines all filters: color, flavour, type, and price range.
    public static Specification<Wine> filter(String color, String flavour, String type, BigDecimal minPrice, BigDecimal maxPrice){
        return Specification.where(hasColor(color))
                .and(hasFlavour(flavour))
                .and(hasType(type))
                .and(hasPriceBetween(minPrice, maxPrice));
    }

}
