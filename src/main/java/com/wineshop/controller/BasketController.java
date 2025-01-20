package com.wineshop.controller;

import com.wineshop.exception.WineNotFoundException;
import com.wineshop.model.BasketItem;
import com.wineshop.model.Wine;
import com.wineshop.service.BasketItemService;
import com.wineshop.service.BasketService;
import com.wineshop.service.WineService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/basket")
public class BasketController {

    private static final Logger logger = LoggerFactory.getLogger(BasketController.class);

    private final int DEFAULT_QUANTITY = 1;

    private final BasketService basketService;
    private final BasketItemService basketItemService;
    private final WineService wineService;

    public BasketController(BasketService basketService, BasketItemService basketItemService, WineService wineService){
        this.basketService = basketService;
        this.basketItemService = basketItemService;
        this.wineService = wineService;
    }

    // Shows basket contents
    @GetMapping
    public String showBasket(HttpSession session, Model model){
        String sessionId = getSessionId(session);
        logger.info("Displaying basket for session: {}", sessionId);

        List<BasketItem> items = basketItemService.getBasketItems(sessionId);
        BigDecimal totalCost = basketItemService.calculateTotalCost(sessionId);

        model.addAttribute("items", items);
        model.addAttribute("totalCost", totalCost);

        return "basket";
    }

    // Adds a product to the basket with default quantity
    @PostMapping("/add")
    public String addToBasket(@RequestParam Integer wineId, HttpSession session, Model model){
        String sessionId = getSessionId(session);
        logger.info("Adding wine {} to basket {}", wineId, sessionId);

        try {
            basketItemService.addOrUpdateBasketItem(sessionId, wineId, DEFAULT_QUANTITY);
            Wine wine = wineService.findWineByIdOrThrow(wineId);
            model.addAttribute("wine", wine);
            return "wine-details";
        } catch (WineNotFoundException e) {
            logger.warn("Wine with ID {} not found, cannot add to basket.", wineId);
            model.addAttribute("errorMessage", "Wine not found.");
            return "wine-details";
        }
    }

    // Removes a product from the basket
    @PostMapping("/remove")
    public String removeFromBasket(@RequestParam Integer wineId, HttpSession session){
        String sessionId = getSessionId(session);
        logger.info("Removing wine {} from basket {}", wineId, sessionId);

        basketItemService.removeBasketItem(sessionId, wineId);

        return "redirect:/basket";
    }

    // Updates item quantities in the basket
    @PostMapping("/update")
    public String updateBasketItemQuantity(
            @RequestParam(required = false) List<Integer> wineIds,
            @RequestParam(required = false) List<Integer> quantities,
            HttpSession session){

        if (wineIds == null || quantities == null) {
            logger.warn("Basket update request received with no products.");
            return "redirect:/basket";
        }

        String sessionId = getSessionId(session);

        for(int i = 0; i < wineIds.size(); i++){
            Integer wineId = wineIds.get(i);
            Integer quantity = quantities.get(i);

            logger.info("Updating wine {} quantity to {} in basket {}", wineIds.get(i), quantities.get(i), sessionId);
            basketItemService.updateBasketItemQuantity(sessionId, wineId, quantity);
        }

        return "redirect:/basket";
    }


    // Retrieves the session ID or generates a new one if it doesn't exist
    private String getSessionId(HttpSession session) {
        String sessionId = (String) session.getAttribute("sessionId");

        if(sessionId == null){
            sessionId = session.getId();
            basketService.getOrCreateBasket(sessionId);
            session.setAttribute("sessionId", sessionId);
        }

        return sessionId;
    }
}
