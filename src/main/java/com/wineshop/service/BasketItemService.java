package com.wineshop.service;

import com.wineshop.exception.BasketItemNotFoundException;
import com.wineshop.exception.BasketNotFoundException;
import com.wineshop.exception.NotEnoughStockException;
import com.wineshop.exception.WineNotFoundException;
import com.wineshop.model.Basket;
import com.wineshop.model.BasketItem;
import com.wineshop.model.Wine;
import com.wineshop.repository.BasketItemRepository;
import com.wineshop.repository.BasketRepository;
import com.wineshop.repository.WineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class BasketItemService {

    private static final Logger logger = LoggerFactory.getLogger(BasketItemService.class);

    private BasketItemRepository basketItemRepository;
    private BasketRepository basketRepository;
    private WineRepository wineRepository;

    public BasketItemService(BasketItemRepository basketItemRepository, BasketRepository basketRepository, WineRepository wineRepository){
        this.basketItemRepository = basketItemRepository;
        this.basketRepository = basketRepository;
        this.wineRepository = wineRepository;
    }

    // Adds a wine to the basket or updates quantity if it already exists
    @Transactional
    public void addOrUpdateBasketItem(String sessionId, Integer wineId, int quantity){
        logger.info("Adding or updating basket item. Session ID: {}, Wine ID: {}, Quantity: {}", sessionId, wineId, quantity);

        Basket basket = findBasketBySessionId(sessionId);
        Wine wine = findWineById(wineId);

        BasketItem basketItem = basketItemRepository.findByBasketAndWine(basket, wine)
                .orElseGet(() -> new BasketItem(wine, quantity, wine.getPrice()));

        if(basketItem.getId() == null){
            basketItem.setBasket(basket);
            logger.info("Created new basket item for wine: {}", wine.getName());
        }else{
            basketItem.setQuantity(basketItem.getQuantity() + quantity);
            logger.info("Updated quantity for basket item. Wine: {}, New Quantity: {}", wine.getName(), basketItem.getQuantity());
        }

        // Ensure the new quantity does not exceed stock availability
        if(basketItem.getQuantity() > wine.getQuantity()){
            logger.warn("Cannot add more than available stock. Wine: {}", wine.getName());
            throw new NotEnoughStockException("Not enough stock for wine: " + wine.getName());
        }

        basketItem.setPrice(wine.getPrice().multiply(BigDecimal.valueOf(basketItem.getQuantity())));
        basketItemRepository.save(basketItem);

        logger.info("Basket item successfully added or updated. Wine: {}", wine.getName());
    }


    // Removes an item from the basket
    @Transactional
    public void removeBasketItem(String sessionId, Integer wineId){
        logger.info("Removing basket item. Session ID: {}, Wine ID: {}", sessionId, wineId);

        Basket basket = findBasketBySessionId(sessionId);
        Wine wine = findWineById(wineId);
        BasketItem basketItem = findBasketItemByBasketAndWine(basket, wine);

        if (basketItem == null) {
            logger.warn("Basket item not found for wine: {}", wine.getName());
            throw new BasketItemNotFoundException("Basket item not found for wine: " + wine.getName());
        }

        basketItemRepository.delete(basketItem);
        logger.info("Basket item removed successfully. Wine: {}", wine.getName());
    }


    //Updates the quantity of an item in the basket
    @Transactional
    public void updateBasketItemQuantity(String sessionId, Integer wineId, Integer newQuantity){
        logger.info("Updating basket item quantity. Session ID: {}, Wine ID: {}, New Quantity: {}", sessionId, wineId, newQuantity);

        Basket basket = findBasketBySessionId(sessionId);
        Wine wine = findWineById(wineId);
        BasketItem basketItem = findBasketItemByBasketAndWine(basket, wine);

        if(newQuantity > wine.getQuantity()){
            logger.warn("Cannot update basket item: Not enough stock. Wine: {} (Requested: {}, Available: {})",
                    wine.getName(), newQuantity, wine.getQuantity());
            throw new NotEnoughStockException("Not enough stock for wine: " + wine.getName());
        }

        basketItem.setQuantity(newQuantity);
        basketItem.setPrice(wine.getPrice().multiply(BigDecimal.valueOf(newQuantity)));
        basketItemRepository.save(basketItem);

        logger.info("Basket item quantity updated successfully. Wine: {}, New Quantity: {}", wine.getName(), newQuantity);
    }

    // Fetches all items in the basket for the given session ID.
    @Transactional(readOnly = true)
    public List<BasketItem> getBasketItems(String sessionId){
        logger.info("Fetching basket items for session ID: {}", sessionId);

        Basket basket = findBasketBySessionId(sessionId);
        List<BasketItem> items = basketItemRepository.findByBasket(basket).stream()
                .sorted(Comparator.comparing(item -> item.getWine().getId()))
                .toList();

        logger.info("Retrieved {} basket items for session ID: {}", items.size(), sessionId);

        return items;
    }

    // Calculates the total cost of items in the basket.
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalCost(String sessionId){
        logger.info("Calculating total cost for session ID: {}", sessionId);

        Basket basket = findBasketBySessionId(sessionId);

        BigDecimal totalCost = basketItemRepository.findByBasket(basket).stream()
                .map(BasketItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal :: add);

        logger.info("Total cost calculated for session ID: {}: {}", sessionId, totalCost);
        return totalCost;
    }


    // Finds a basket by session ID or throws a BasketNotFoundException if not found.
    private Basket findBasketBySessionId(String sessionId){
        return basketRepository.findBySessionId(sessionId)
                .orElseThrow(() -> {
                    logger.warn("Basket not found for session ID: {}", sessionId);
                    return new BasketNotFoundException("Basket not found for session ID: " + sessionId);
                });
    }

    // Finds a wine by ID or throws a WineNotFoundException if not found.
    private Wine findWineById(Integer wineId){
        return wineRepository.findById(wineId)
                .orElseThrow(() -> {
                    logger.warn("Wine not found for ID: {}", wineId);
                    return new WineNotFoundException("Wine not found for ID: " + wineId);
                });
    }

    // Finds a basket item by basket and wine or throws a BasketItemNotFoundException if not found.
    private BasketItem findBasketItemByBasketAndWine (Basket basket, Wine wine){
        return basketItemRepository.findByBasketAndWine(basket, wine)
                .orElseThrow(() -> {
                    logger.warn("Basket item not found for wine: {}", wine.getName());
                    return new BasketItemNotFoundException("Basket item not found for wine: " + wine.getName());
                });
    }


}
