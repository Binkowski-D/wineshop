package com.wineshop.controller;

import com.wineshop.exception.WineNotFoundException;
import com.wineshop.model.Wine;
import com.wineshop.service.WineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;


@Controller
@RequestMapping("/")
public class WineController {

    private static final Logger logger = LoggerFactory.getLogger(WineController.class);
    private final WineService wineService;

    public WineController(WineService wineService){
        this.wineService = wineService;
    }

    // Displays the home page with empty wine list
    @GetMapping
    public String showHomePage(Model model){
        logger.info("Opening home page.");
        model.addAttribute("wines", List.of());
        model.addAttribute("searched", false);

        return "index";
    }


    // Filter wines based on optional criteria
    @GetMapping("/wines")
    public String filterWines(
            @RequestParam(required = false)String color,
            @RequestParam(required = false)String flavour,
            @RequestParam(required = false)String type,
            @RequestParam(required = false)String price,
            Model model){

        if (color != null || flavour != null || type != null || price != null) {
            logger.info("Filtering wines - Color: {}, Flavour: {}, Type: {}, Price: {}", color, flavour, type, price);
        } else {
            logger.info("Filtering request received, but no criteria provided.");
        }

        model.addAttribute("color", color);
        model.addAttribute("flavour", flavour);
        model.addAttribute("type", type);
        model.addAttribute("price", price);

        List<Wine> filteredWines = wineService.filterWines(color, flavour, type, price);
        model.addAttribute("wines", filteredWines);
        model.addAttribute("searched", true);

        logger.info("Found {} wines.", filteredWines.size());

        return "index";
    }

    // Displays details of a wine by ID
    @GetMapping("/wines/{id}")
    public String showWineDetails(@PathVariable Integer id, Model model){
        logger.info("Fetching details for wine with ID: {}", id);

        try {
            Wine wine = wineService.findWineByIdOrThrow(id);
            model.addAttribute("wine", wine);
            logger.info("Loaded wine details: {}", wine.getName());
            return "wine-details";
        }catch (WineNotFoundException ex){
            model.addAttribute("errorMessage", "Wine not found");
            logger.warn("Wine with ID {} not found.", id);
            return "wine-details";
        }
    }
}
