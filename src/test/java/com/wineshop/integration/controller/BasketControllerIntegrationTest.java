package com.wineshop.integration.controller;

import com.wineshop.model.BasketItem;
import com.wineshop.model.Wine;
import com.wineshop.repository.BasketItemRepository;
import com.wineshop.repository.BasketRepository;
import com.wineshop.repository.WineRepository;
import com.wineshop.service.BasketService;
import com.wineshop.util.BaseTestSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BasketControllerIntegrationTest extends BaseTestSetup {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WineRepository wineRepository;

    @Autowired
    private BasketItemRepository basketItemRepository;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private BasketService basketService;

    private Wine wine;

    @BeforeEach
    void setUp(){
        basketItemRepository.deleteAll();
        basketRepository.deleteAll();
        wineRepository.deleteAll();

        initTestData();

        wine = wineRepository.findAll().get(0);
        basketService.getOrCreateBasket("abc123");
    }

    // Test if an empty basket is displayed correctly.
    @Test
    public void testShouldDisplayEmptyBasket() throws Exception {
        mockMvc.perform(get("/basket").sessionAttr("sessionId", "abc123"))
                .andExpect(status().isOk())
                .andExpect(view().name("basket"))
                .andExpect(model().attributeExists("items", "totalCost"))
                .andExpect(model().attribute("items", List.of()))
                .andExpect(model().attribute("totalCost", BigDecimal.ZERO));
    }

    // Test if a product is successfully added to the basket.
    @Test
    public void testShouldAddProductToBasket() throws Exception{
        mockMvc.perform(post("/basket/add")
                .param("wineId", String.valueOf(wine.getId())).sessionAttr("sessionId", "abc123"))
                .andExpect(status().isOk())
                .andExpect(view().name("wine-details"));

        List<BasketItem> items = basketItemRepository.findAll();
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getWine()).isEqualTo(wine);
        assertThat(items.get(0).getQuantity()).isEqualTo(1);

    }

    // Test if a product is successfully removed from the basket.
    @Test
    public void testShouldRemoveProductFromBasket() throws Exception {
        mockMvc.perform(post("/basket/add")
                .param("wineId", String.valueOf(wine.getId())).sessionAttr("sessionId", "abc123"));

        mockMvc.perform(post("/basket/remove")
                .param("wineId", String.valueOf(wine.getId()))
                .sessionAttr("sessionId", "abc123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/basket"));

        List<BasketItem> items = basketItemRepository.findAll();
        assertThat(items).isEmpty();
    }

    // Test if the basket item quantity is successfully updated.
    @Test
    public void testShouldUpdateBasketItemQuantity() throws Exception{
        mockMvc.perform(post("/basket/add")
                .param("wineId", String.valueOf(wine.getId())).sessionAttr("sessionId", "abc123"));

        mockMvc.perform(post("/basket/update")
                        .param("wineIds", String.valueOf(wine.getId()))
                        .param("quantities", "3")
                        .sessionAttr("sessionId", "abc123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/basket"));

        List<BasketItem> items = basketItemRepository.findAll();
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getQuantity()).isEqualTo(3);

    }

    // Test if multiple products are successfully added to the basket.
    @Test
    public void testShouldAddMultipleProductsToBasket() throws Exception {
        Wine secondWine = wineRepository.findAll().get(1);

        mockMvc.perform(post("/basket/add")
                .param("wineId", String.valueOf(wine.getId())).sessionAttr("sessionId", "abc123"));

        mockMvc.perform(post("/basket/add")
                .param("wineId", String.valueOf(secondWine.getId())).sessionAttr("sessionId", "abc123"));

        List<BasketItem> items = basketItemRepository.findAll();
        assertThat(items).hasSize(2);
        assertThat(items.stream().map(BasketItem::getWine)).containsExactlyInAnyOrder(wine, secondWine);
    }

    // Test if the basket total cost is correctly calculated.
    @Test
    public void testShouldCalculateTotalCostCorrectly() throws Exception {
        Wine secondWine = wineRepository.findAll().get(1);

        mockMvc.perform(post("/basket/add")
                .param("wineId", String.valueOf(wine.getId())).sessionAttr("sessionId", "abc123"));

        mockMvc.perform(post("/basket/add")
                .param("wineId", String.valueOf(secondWine.getId())).sessionAttr("sessionId", "abc123"));

        mockMvc.perform(get("/basket").sessionAttr("sessionId", "abc123"))
                .andExpect(model().attribute("totalCost", wine.getPrice().add(secondWine.getPrice())));
    }

    // Test if trying to add a non-existing product returns a bad request.
    @Test
    public void testShouldReturnBadRequestWhenAddingNonExistingProduct() throws Exception{
        mockMvc.perform(post("/basket/add")
                        .param("wineId", "99999") // Non-existent ID
                        .sessionAttr("sessionId", "abc123"))
                .andExpect(model().attributeExists("errorMessage"));

    }
}
