package pe.com.birdcare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Builder
public class Product implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotBlank
    @Column(length = 100,nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT",nullable = true)
    @Builder.Default
    private String description = "No description yet for this product.";

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 10,scale = 2)
    private BigDecimal price;

    @NotNull
    @Column(nullable = false)
    @PositiveOrZero
    private Integer stock;
}
