package com.wineshop.unit.service;

import com.wineshop.model.Basket;
import com.wineshop.repository.BasketRepository;
import com.wineshop.service.BasketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class BasketServiceTest {

    private BasketRepository basketRepository;
    private BasketService basketService;

    // Initialize mocked repository and service
    @BeforeEach
    void setUp(){
        basketRepository = Mockito.mock(BasketRepository.class);
        basketService = new BasketService(basketRepository);
    }

    // Tests returning an existing basket if found by session ID
    @Test
    void testShouldReturnExistingBasketIfFound(){
        String sessionId = "abc123";
        Basket existingBasket = createBasket(sessionId);

        when(basketRepository.findBySessionId(sessionId)).thenReturn(Optional.of(existingBasket));

        Basket result = basketService.getOrCreateBasket(sessionId);

        assertThat(result).isEqualTo(existingBasket);
        verify(basketRepository).findBySessionId(sessionId);
        verifyNoMoreInteractions(basketRepository);
    }

    // Tests creating a new basket if none exists for the session
    @Test
    void testShouldCreateNewBasketIfNotFound(){
        String sessionId = "abc123";
        when(basketRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        basketService.getOrCreateBasket(sessionId);

        ArgumentCaptor<Basket> captor = ArgumentCaptor.forClass(Basket.class);
        verify(basketRepository).save(captor.capture());

        Basket savedBasket = captor.getValue();
        assertThat(savedBasket.getSessionId()).isEqualTo(sessionId);
    }

    // Tests deleting an existing basket
    @Test
    void testShouldDeleteExistingBasket() {
        String sessionId = "abc123";
        Basket existingBasket = createBasket(sessionId);

        when(basketRepository.findBySessionId(sessionId)).thenReturn(Optional.of(existingBasket));

        basketService.deleteBasket(sessionId);

        verify(basketRepository, times(1)).findBySessionId(sessionId);
        verify(basketRepository, times(1)).delete(existingBasket);
    }

    // Tests that no action is taken if the basket does not exist
    @Test
    void testShouldNotDeleteBasketIfNotFound(){
        String sessionId = "abc123";
        when(basketRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        basketService.deleteBasket(sessionId);

        verify(basketRepository).findBySessionId(sessionId);
        verify(basketRepository, never()).delete(any());
    }

    // Tests updating the session ID for an existing basket
    @Test
    void testShouldUpdateSessionIdForExistingBasket(){
        String oldSession = "abc123";
        String newSession = "def456";

        Basket existingBasket = createBasket(oldSession);
        when(basketRepository.findBySessionId(oldSession)).thenReturn(Optional.of(existingBasket));

        basketService.updateSessionId(oldSession, newSession);

        assertThat(existingBasket.getSessionId()).isEqualTo(newSession);
        verify(basketRepository).findBySessionId(oldSession);
        verify(basketRepository).save(existingBasket);
    }

    // Tests that session ID is not updated if the basket does not exist
    @Test
    void testShouldNotUpdateSessionIdIfBasketNotFound(){
        String oldSession = "abc123";
        String newSession = "def456";

        when(basketRepository.findBySessionId(oldSession)).thenReturn(Optional.empty());

        basketService.updateSessionId(oldSession, newSession);

        verify(basketRepository).findBySessionId(oldSession);
        verify(basketRepository, never()).save(any());
    }

    private Basket createBasket(String sessionId) {
        return new Basket(sessionId);
    }

}
