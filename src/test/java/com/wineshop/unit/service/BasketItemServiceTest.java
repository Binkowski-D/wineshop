package com.wineshop.unit.service;

import com.wineshop.exception.BasketItemNotFoundException;
import com.wineshop.exception.NotEnoughStockException;
import com.wineshop.model.Basket;
import com.wineshop.model.BasketItem;
import com.wineshop.model.Wine;
import com.wineshop.repository.BasketItemRepository;
import com.wineshop.repository.BasketRepository;
import com.wineshop.repository.WineRepository;
import com.wineshop.service.BasketItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class BasketItemServiceTest {

    private BasketItemService basketItemService;
    private BasketItemRepository basketItemRepository;
    private BasketRepository basketRepository;
    private WineRepository wineRepository;

    @BeforeEach
    void setUp(){
        basketItemRepository = Mockito.mock(BasketItemRepository.class);
        basketRepository = Mockito.mock(BasketRepository.class);
        wineRepository = Mockito.mock(WineRepository.class);

        basketItemService = new BasketItemService(basketItemRepository, basketRepository, wineRepository);
    }

    // Test adding a new item to the basket when it does not exist yet
    @Test
    public void testShouldAddNewBasketItemWhenNotExists(){
        String sessionId = "abc123";
        Integer wineId = 1;
        int quantity = 2;

        Basket basket = new Basket(sessionId);
        Wine wine = new Wine("Cabernet Sauvignon", BigDecimal.valueOf(50), "image.jpg", 750, 10, null, null);

        when(basketRepository.findBySessionId(sessionId)).thenReturn(Optional.of(basket));
        when(wineRepository.findById(wineId)).thenReturn(Optional.of(wine));
        when(basketItemRepository.findByBasketAndWine(basket, wine)).thenReturn(Optional.empty());

        basketItemService.addOrUpdateBasketItem(sessionId, wineId, quantity);

        ArgumentCaptor<BasketItem> captor = ArgumentCaptor.forClass(BasketItem.class);
        verify(basketItemRepository).save(captor.capture());

        BasketItem savedItem = captor.getValue();

        assertThat(savedItem.getWine()).isEqualTo(wine);
        assertThat(savedItem.getQuantity()).isEqualTo(2);
        assertThat(savedItem.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    // Test ensuring an exception is thrown when trying to add more wine than available in stock
    @Test
    public void testShouldThrowExceptionWhenStockIsInsufficient(){
        String sessionId = "abc123";
        Integer wineId = 1;
        int quantity = 15;

        Basket basket = new Basket(sessionId);
        Wine wine = new Wine("Cabernet Sauvignon", BigDecimal.valueOf(50), "image.jpg", 750, 10, null, null);

        when(basketRepository.findBySessionId(sessionId)).thenReturn(Optional.of(basket));
        when(wineRepository.findById(wineId)).thenReturn(Optional.of(wine));
        when(basketItemRepository.findByBasketAndWine(basket, wine)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> basketItemService.addOrUpdateBasketItem(sessionId, wineId, quantity))
                .isInstanceOf(NotEnoughStockException.class)
                .hasMessageContaining("Not enough stock");

        verify(basketItemRepository, never()).save(any(BasketItem.class));

    }

    // Test removing an existing item from the basket
    @Test
    public void testShouldRemoveBasketItem(){
        String sessionId = "abc123";
        Integer wineId = 1;

        Basket basket = new Basket(sessionId);
        Wine wine = new Wine("Cabernet Sauvignon", BigDecimal.valueOf(50), "image.jpg", 750, 10, null, null);
        BasketItem basketItem = new BasketItem(wine, 2, BigDecimal.valueOf(100));
        basketItem.setBasket(basket);

        when(basketRepository.findBySessionId(sessionId)).thenReturn(Optional.of(basket));
        when(wineRepository.findById(wineId)).thenReturn(Optional.of(wine));
        when(basketItemRepository.findByBasketAndWine(basket, wine)).thenReturn(Optional.of(basketItem));

        basketItemService.removeBasketItem(sessionId, wineId);

        verify(basketItemRepository).delete(basketItem);
    }

    // Test not removing a basket item if it does not exist in the basket
    @Test
    public void testShouldNotRemoveBasketItemIfDoesNotExist(){
        String sessionId = "abc123";
        Integer wineId = 1;

        Basket basket = new Basket(sessionId);
        Wine wine = new Wine("Cabernet Sauvignon", BigDecimal.valueOf(50), "image.jpg", 750, 10, null, null);

        when(basketRepository.findBySessionId(sessionId)).thenReturn(Optional.of(basket));
        when(wineRepository.findById(1)).thenReturn(Optional.of(wine));
        when(basketItemRepository.findByBasketAndWine(basket, wine)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> basketItemService.removeBasketItem(sessionId, wineId))
                .isInstanceOf(BasketItemNotFoundException.class)
                .hasMessageContaining("Basket item not found for wine");

        verify(basketItemRepository, never()).delete(any(BasketItem.class));
    }

    // Test updating the quantity of an existing item in the basket
    @Test
    public void testShouldUpdateBasketItemQuantity() {
        String sessionId = "abc123";
        Integer wineId = 1;
        int newQuantity = 3;

        Basket basket = new Basket(sessionId);
        Wine wine = new Wine("Cabernet Sauvignon", BigDecimal.valueOf(50), "image.jpg", 750, 10, null, null);
        BasketItem basketItem = new BasketItem(wine, 2, BigDecimal.valueOf(100));
        basketItem.setBasket(basket);

        when(basketRepository.findBySessionId(sessionId)).thenReturn(Optional.of(basket));
        when(wineRepository.findById(wineId)).thenReturn(Optional.of(wine));
        when(basketItemRepository.findByBasketAndWine(basket, wine)).thenReturn(Optional.of(basketItem));

        basketItemService.updateBasketItemQuantity(sessionId, wineId, newQuantity);

        ArgumentCaptor<BasketItem> captor = ArgumentCaptor.forClass(BasketItem.class);
        verify(basketItemRepository).save(captor.capture());

        BasketItem updatedItem = captor.getValue();

        assertThat(updatedItem.getQuantity()).isEqualTo(3);
        assertThat(updatedItem.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(150));
    }

    // Test retrieving all basket items for a session
    @Test
    void shouldRetrieveBasketItems() {
        String sessionId = "abc123";
        Basket basket = new Basket(sessionId);

        BasketItem item1 = new BasketItem(new Wine("Wine A", BigDecimal.valueOf(30), "image1.jpg", 750, 10, null, null), 2, BigDecimal.valueOf(60));
        BasketItem item2 = new BasketItem(new Wine("Wine B", BigDecimal.valueOf(40), "image2.jpg", 750, 8, null, null), 1, BigDecimal.valueOf(40));

        when(basketRepository.findBySessionId(sessionId)).thenReturn(Optional.of(basket));
        when(basketItemRepository.findByBasket(basket)).thenReturn(List.of(item1, item2));

        List<BasketItem> items = basketItemService.getBasketItems(sessionId);
        assertThat(items).hasSize(2).containsExactlyInAnyOrder(item1, item2);
    }
}
