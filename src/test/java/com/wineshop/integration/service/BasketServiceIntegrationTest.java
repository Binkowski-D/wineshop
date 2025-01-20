package com.wineshop.integration.service;

import com.wineshop.model.Basket;
import com.wineshop.repository.BasketRepository;
import com.wineshop.service.BasketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class BasketServiceIntegrationTest {

    @Autowired
    private BasketService basketService;

    @Autowired
    private BasketRepository basketRepository;

    @BeforeEach
    void setUp(){
        // Clear database before each test
        basketRepository.deleteAll();
    }

    // Tests if a basket is created and retrieved correctly
    @Test
    void testShouldCreateAndRetrieveBasket(){
        String sessionId = "abc123";

        Basket createdBasket = basketService.getOrCreateBasket(sessionId);

        assertThat(createdBasket.getSessionId()).isEqualTo(sessionId);
        assertThat(basketRepository.findBySessionId(sessionId)).isPresent();
    }

    // Tests deleting a basket
    @Test
    void testShouldDeleteBasket(){
        String sessionId = "abc123";
        Basket basket = basketService.getOrCreateBasket(sessionId);

        basketService.deleteBasket(sessionId);

        assertThat(basketRepository.findBySessionId(sessionId)).isEmpty();
    }

    // Tests updating the session ID of a basket
    @Test
    void testShouldUpdateSessionId(){
        String oldSessionId = "abc";
        String newSessionId = "123";

        Basket basket = basketService.getOrCreateBasket(oldSessionId);

        basketService.updateSessionId(oldSessionId, newSessionId);

        assertThat(basketRepository.findBySessionId(oldSessionId)).isEmpty();
        assertThat(basketRepository.findBySessionId(newSessionId)).isPresent();
    }
}
