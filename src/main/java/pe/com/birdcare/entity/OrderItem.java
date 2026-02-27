package pe.com.birdcare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter @Setter
public class OrderItem implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="product_id",nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal price;

    public OrderItem(Product product, Integer quantity, BigDecimal priceAtThisMoment) {
        if (product == null) throw new IllegalArgumentException("Product is mandatory");
        if (quantity == null || quantity <= 0)
            throw new IllegalArgumentException("Quantity must be greater than zero");
        if (priceAtThisMoment == null || priceAtThisMoment.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Price must be positive");

        this.product = product;
        this.quantity = quantity;
        this.price = priceAtThisMoment;
    }

    public BigDecimal getSubtotal() {
        if (this.price == null || this.quantity == null) return BigDecimal.ZERO;
        return this.price.multiply(BigDecimal.valueOf(this.quantity));
    }
}
