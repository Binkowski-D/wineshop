package com.wineshop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.math.BigDecimal;


@Entity
@Table(name = "basket_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"basket_id", "wine_id"})
})
@NoArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BasketItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "basket_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    @Setter
    private Basket basket;

    @ManyToOne
    @JoinColumn(name="wine_id", nullable = false)
    @NotNull
    @Setter
    private Wine wine;

    @Column(nullable = false)
    @NotNull
    @Setter
    private Integer quantity = 1;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull
    @Setter
    private BigDecimal price;

    public BasketItem(Wine wine, Integer quantity, BigDecimal price){
        this.wine = wine;
        this.quantity = quantity;
        this.price = price;
    }
}
