package pe.com.birdcare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private Integer stock;

    @Builder
    private Product(String name, String description, BigDecimal price, Integer stock, Category category, Boolean active) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.active = active;
    }

    public static Product create(String name, String description, BigDecimal price, Integer stock, Category category){
        validateName(name);
        validatePrice(price);
        validateStock(stock);
        validateCategory(category);
        return Product.builder()
                .name(name)
                .description(description == null ? "No description yet for this product." : description)
                .price(price)
                .stock(stock)
                .category(category)
                .active(true)
                .build();
    }

    public void updateDetails(String name, String description, BigDecimal price, Category category){
        validateName(name);
        validatePrice(price);
        validateCategory(category);
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    public void enable(){
        this.active = true;
    }

    public void disable(){
        this.active = false;
    }

    public void decreaseStock(int quantity){
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (this.stock < quantity) {
            throw new IllegalStateException("Not enough stock for: " + this.name);
        }
        this.stock -= quantity;
    }

    public void restoreStock(int quantity){
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.stock += quantity;
    }

    private static void validateName(String name){
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
    }

    private static void validatePrice(BigDecimal price){
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }

    private static void validateStock(Integer stock){
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
    }

    private static void validateCategory(Category category){
        if (category == null) {
            throw new IllegalArgumentException("Category is required");
        }
    }
}
