package com.wineshop.integration.service;
import com.wineshop.exception.NotEnoughStockException;
import com.wineshop.model.Basket;
import com.wineshop.model.BasketItem;
import com.wineshop.model.Wine;
import com.wineshop.repository.BasketItemRepository;
import com.wineshop.repository.BasketRepository;
import com.wineshop.repository.WineRepository;
import com.wineshop.service.BasketItemService;
import com.wineshop.util.BaseTestSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class BasketItemServiceIntegrationTest extends BaseTestSetup {

    @Autowired
    BasketItemService basketItemService;

    @Autowired
    BasketItemRepository basketItemRepository;

    @Autowired
    BasketRepository basketRepository;

    @Autowired
    WineRepository wineRepository;

    private Basket basket;

    private Wine wine;

    @BeforeEach
    void setUp(){
        basketItemRepository.deleteAll();
        basketRepository.deleteAll();
        wineRepository.deleteAll();

        initTestData();

        basket = basketRepository.save(new Basket("abc123"));
        wine = wineRepository.findAll().get(0);
    }

    // Test adding a new wine to the basket when it does not exist
    @Test
    public void testShouldAddNewBasketItem(){
        basketItemService.addOrUpdateBasketItem(basket.getSessionId(), wine.getId(), 2);

        List<BasketItem> items = basketItemRepository.findByBasket(basket);
        assertThat(items).hasSize(1);

        BasketItem basketItem = items.get(0);
        assertThat(basketItem.getWine().getName()).isEqualTo(wine.getName());
        assertThat(basketItem.getQuantity()).isEqualTo(2);
        assertThat(basketItem.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(160));

    }

    // Test increasing the quantity of an existing item in the basket
    @Test
    public void testShouldUpdateExistingBasketItem(){
        basketItemService.addOrUpdateBasketItem(basket.getSessionId(), wine.getId(), 2);

        basketItemService.addOrUpdateBasketItem(basket.getSessionId(), wine.getId(), 3);

        List<BasketItem> items = basketItemRepository.findByBasket(basket);
        assertThat(items).hasSize(1);

        BasketItem basketItem = items.get(0);
        assertThat(basketItem.getWine().getName()).isEqualTo(wine.getName());
        assertThat(basketItem.getQuantity()).isEqualTo(5);
        assertThat(basketItem.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(400));
    }

    // Test ensuring an exception is thrown when adding more than available stock
    @Test
    public void testShouldThrowExceptionForInsufficientStock(){
        assertThatThrownBy(() -> basketItemService.addOrUpdateBasketItem(basket.getSessionId(), wine.getId(), 20))
                .isInstanceOf(NotEnoughStockException.class)
                .hasMessageContaining("Not enough stock");

        assertThat(basketItemRepository.findByBasket(basket)).isEmpty();

    }

    // Test removing an existing item from the basket
    @Test
    public void testShouldRemoveBasketItem(){
        basketItemService.addOrUpdateBasketItem(basket.getSessionId(), wine.getId(), 5);

        basketItemService.removeBasketItem(basket.getSessionId(), wine.getId());

        List<BasketItem> items = basketItemRepository.findByBasket(basket);
        assertThat(items).isEmpty();

    }

    // Test changing the quantity of an item in the basket
    @Test
    public void testShouldUpdateBasketItemQuantity(){
        basketItemService.addOrUpdateBasketItem(basket.getSessionId(), wine.getId(), 5);

        basketItemService.updateBasketItemQuantity(basket.getSessionId(), wine.getId(), 3);

        List<BasketItem> items = basketItemRepository.findByBasket(basket);
        BasketItem basketItem = items.get(0);

        assertThat(basketItem.getQuantity()).isEqualTo(3);
        assertThat(basketItem.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(240));
    }

    // Test fetching all items in the basket
    @Test
    void testShouldRetrieveAllBasketItems(){
        basketItemService.addOrUpdateBasketItem(basket.getSessionId(), wine.getId(), 5);

        Wine anotherWine = wineRepository.findAll().get(1);

        basketItemService.addOrUpdateBasketItem(basket.getSessionId(), anotherWine.getId(), 1);

        List<BasketItem> items = basketItemService.getBasketItems(basket.getSessionId());

        assertThat(items).hasSize(2);
        assertThat(items).anyMatch(item -> item.getWine().equals(wine) && item.getQuantity() == 5);
        assertThat(items).anyMatch(item -> item.getWine().equals(anotherWine) && item.getQuantity() == 1);

    }

    // Test calculating the total cost of all items in the basket
    @Test
    void testShouldCalculateTotalBasketCost(){
        basketItemService.addOrUpdateBasketItem(basket.getSessionId(), wine.getId(), 3);

        Wine anotherWine = wineRepository.findAll().get(1);

        basketItemService.addOrUpdateBasketItem(basket.getSessionId(), anotherWine.getId(), 2);

        BigDecimal totalCost = basketItemService.calculateTotalCost(basket.getSessionId());

        assertThat(totalCost).isEqualByComparingTo(BigDecimal.valueOf(240).add(BigDecimal.valueOf(80)));
    }

}