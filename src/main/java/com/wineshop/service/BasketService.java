package com.wineshop.service;

import com.wineshop.model.Basket;
import com.wineshop.repository.BasketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BasketService {

    private static final Logger logger = LoggerFactory.getLogger(BasketService.class);

    private final BasketRepository basketRepository;

    public BasketService(BasketRepository basketRepository){
        this.basketRepository = basketRepository;
    }

    // Retrieves an existing basket or creates a new one for the session
    public Basket getOrCreateBasket(String sessionId){

        Optional<Basket> existingBasket = basketRepository.findBySessionId(sessionId);

        if (existingBasket.isPresent()) {
            logger.info("Using existing basket for sessionId: {}", sessionId);
            return existingBasket.get();
        }

        Basket newBasket = basketRepository.save(new Basket(sessionId));
        logger.info("Created new basket for sessionId: {}", sessionId);
        return newBasket;

    }

    // Deletes the basket associated with the given session ID
    public void deleteBasket(String sessionId){
        Optional<Basket> existingBasket = basketRepository.findBySessionId(sessionId);

        if(existingBasket.isPresent()){
            basketRepository.delete(existingBasket.get());
            logger.info("Deleted basket for sessionId: {}", sessionId);
        }else {
            logger.warn("No basket found for sessionId: {}. Nothing to delete.", sessionId);
        }
    }

    // Updates session ID for an existing basket
    public void updateSessionId(String oldSessionId, String newSessionId){
        Optional<Basket> existingBasket = basketRepository.findBySessionId(oldSessionId);

        if(existingBasket.isPresent()){
            Basket basket = existingBasket.get();
            basket.setSessionId(newSessionId);
            basketRepository.save(basket);
            logger.info("Updated sessionId: {} → {}", oldSessionId, newSessionId);
        }else {
            logger.warn("No basket found for sessionId: {}", oldSessionId);
        }
    }
}
