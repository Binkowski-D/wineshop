package com.wineshop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;


@Entity
@Table(name = "wines")
@NoArgsConstructor
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Wine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @NotNull
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    @Setter
    private String name;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 10, scale = 2)
    @Setter
    private BigDecimal price;

    @NotNull
    @Column(nullable = false)
    @Setter
    private String imagePath;

    @Setter
    private String description;

    @Setter
    private String pairing;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Digits(integer = 3, fraction = 1)
    @Setter
    private BigDecimal alcoholContent;

    @NotNull
    @Min(187)
    @Column(nullable = false)
    @Setter
    private Integer volume;

    @Size(max = 50)
    @Column(length = 50)
    @Setter
    private String servingTemperature;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    @Setter
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "color_id", nullable = false)
    @Setter
    private Color color;

    @ManyToOne
    @JoinColumn(name = "flavour_id", nullable = false)
    @Setter
    private Flavour flavour;

    @ManyToOne
    @JoinColumn(name = "type_id")
    @Setter
    private Type type;

    @ManyToOne
    @JoinColumn(name = "grape_variety_id")
    @Setter
    private Grape grape;

    public Wine(String name, BigDecimal price, String imagePath, Integer volume, Integer quantity, Color color, Flavour flavour) {
        this.name = name;
        this.price = price;
        this.imagePath = imagePath;
        this.volume = volume;
        this.quantity = quantity;
        this.color = color;
        this.flavour = flavour;
    }

    public Wine(String name, BigDecimal price, String imagePath, String description, String pairing, BigDecimal alcoholContent,
                Integer volume, String servingTemperature, Integer quantity, Color color, Flavour flavour, Type type,
                Grape grape) {
        this(name, price, imagePath, volume, quantity, color, flavour);
        this.description = description;
        this.pairing = pairing;
        this.alcoholContent = alcoholContent;
        this.servingTemperature = servingTemperature;
        this.type = type;
        this.grape = grape;
    }

}
