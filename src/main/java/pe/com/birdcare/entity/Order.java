package pe.com.birdcare.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.com.birdcare.enums.OrderStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String shippingAddress;

    @Column(nullable = false, updatable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OrderStatus status; // Set a default!

    public Order(User user, String shippingAddress) {
        if (user == null) throw new IllegalArgumentException("Not user for the order");
        if (shippingAddress == null || shippingAddress.isBlank())
            throw new IllegalArgumentException("Shipping address is obligatory.");
        this.user = user;
        this.shippingAddress = shippingAddress;
        this.status = OrderStatus.PENDING;
        this.total = BigDecimal.ZERO;
        this.items = new ArrayList<>();
    }

    @PrePersist
    protected void onCreate() {
        this.orderDate = LocalDateTime.now();
    }

    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
        this.total =
                this.total.add(
                        item.getPrice().multiply(
                                BigDecimal.valueOf(item.getQuantity())
                        )
                );
    }
}
