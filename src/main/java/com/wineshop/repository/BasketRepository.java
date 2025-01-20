package com.wineshop.repository;

import com.wineshop.model.Basket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BasketRepository extends JpaRepository<Basket, Integer> {

    Optional<Basket> findBySessionId(String sessionId);
}
