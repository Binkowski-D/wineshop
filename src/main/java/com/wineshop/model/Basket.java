package com.wineshop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="baskets")
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Basket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name="session_id", nullable = false, unique = true)
    @NotNull
    @NonNull
    @Setter
    private String sessionId;

    @OneToMany(mappedBy = "basket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BasketItem> items = new ArrayList<>();

    public void addItem(BasketItem item){
        this.items.add(item);
        item.setBasket(this);
    }

    public void removeItem(BasketItem item) {
        this.items.remove(item);
        item.setBasket(null);
    }
}
