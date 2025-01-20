package com.wineshop.util;

import com.wineshop.model.*;
import com.wineshop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

// BaseTestSetup initializes test data for Wine and related entities for testing.
public abstract class BaseTestSetup {

    @Autowired
    protected ColorRepository colorRepository;

    @Autowired
    protected FlavourRepository flavourRepository;

    @Autowired
    protected GrapeRepository grapeRepository;

    @Autowired
    protected TypeRepository typeRepository;

    @Autowired
    protected WineRepository wineRepository;

    protected void initTestData(){

        // Deletes all existing data before inserting test records.
        wineRepository.deleteAll();
        colorRepository.deleteAll();
        flavourRepository.deleteAll();
        typeRepository.deleteAll();
        grapeRepository.deleteAll();

        // Initialize test colors
        Color red = colorRepository.save(new Color("Red"));
        Color white = colorRepository.save(new Color("White"));
        Color pink = colorRepository.save(new Color("Pink"));

        // Initialize test flavours
        Flavour dry = flavourRepository.save(new Flavour("Dry"));
        Flavour semiDry = flavourRepository.save(new Flavour("Semi-Dry"));
        Flavour semiSweet = flavourRepository.save(new Flavour("Semi-Sweet"));
        Flavour sweet = flavourRepository.save(new Flavour("Sweet"));

        // Initialize test types
        Type sparkling = typeRepository.save(new Type("Sparkling"));

        // Initialize test grapes
        Grape glera = grapeRepository.save(new Grape("Glera"));

        // Add test wines
        addWine("Cabernet Sauvignon", BigDecimal.valueOf(80), "", 750, 10, red, dry);

        addWine("Merlot", BigDecimal.valueOf(40), "", 750, 5, red, dry);

        addWine("Chardonnay", BigDecimal.valueOf(25), "", 750, 15, white, semiDry);

        addWine("Sauvignon Blanc", BigDecimal.valueOf(65), "", 750, 9, white, dry);

        addWine("Pinot Noir", BigDecimal.valueOf(18), "", 750, 30, pink, sweet);

        addWine("Prosecco", BigDecimal.valueOf(60), "", "", "",
                BigDecimal.valueOf(10), 750, "", 25, white, dry, sparkling, glera);

        addWine("Kindzmarauli", BigDecimal.valueOf(34), "", 750, 15, red, sweet);

        addWine("Rkatsiteli", BigDecimal.valueOf(25), "", 750, 25, white, semiDry);

        addWine("ERA Cabernet", BigDecimal.valueOf(20), "", 750, 19, red, semiSweet);


    }

    // Helper method to create and save simple Wine objects
    private void addWine(String name, BigDecimal price, String imagePath, Integer volume, Integer quantity, Color color, Flavour flavour){
        wineRepository.save(new Wine(name, price, imagePath, volume, quantity, color, flavour));
    }

    // Helper method to create and save Wine objects with additional attributes
    private void addWine(String name, BigDecimal price, String imagePath, String description, String pairing, BigDecimal alcoholContent,
                         Integer volume, String servingTemperature, Integer quantity, Color color, Flavour flavour, Type type,
                         Grape grape){
        wineRepository.save(new Wine(name, price, imagePath, description, pairing, alcoholContent, volume, servingTemperature, quantity, color,
                flavour, type, grape));
    }
}
