package com.wineshop.repository;

import com.wineshop.model.Basket;
import com.wineshop.model.BasketItem;
import com.wineshop.model.Wine;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface BasketItemRepository extends JpaRepository<BasketItem, Integer> {

    // Find a specific item in a basket by wine
    Optional<BasketItem> findByBasketAndWine(Basket basket, Wine wine);

    // Find all items in a specific basket
    List<BasketItem> findByBasket(Basket basket);

    // Delete all items in a specific basket
    void deleteByBasket(Basket basket);
}
