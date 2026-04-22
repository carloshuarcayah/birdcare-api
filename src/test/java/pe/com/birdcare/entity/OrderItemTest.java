package pe.com.birdcare.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderItemTest {

    private Product product;

    @BeforeEach
    void setUp() {
        Category category = new Category("Alimento", "Comida");
        product = Product.create("Semillas", "Mix", new BigDecimal("25.00"), 10, category);
    }

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        void storesProductQuantityAndPriceAtThisMoment() {
            BigDecimal priceNow = new BigDecimal("25.00");

            OrderItem item = new OrderItem(product, 3, priceNow);

            assertThat(item.getProduct()).isEqualTo(product);
            assertThat(item.getQuantity()).isEqualTo(3);
            assertThat(item.getPrice()).isEqualByComparingTo(priceNow);
        }

        @Test
        void rejectsNullProduct() {
            assertThatThrownBy(() -> new OrderItem(null, 3, new BigDecimal("25.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Product");
        }

        @Test
        void rejectsZeroQuantity() {
            assertThatThrownBy(() -> new OrderItem(product, 0, new BigDecimal("25.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity");
        }

        @Test
        void rejectsNegativeQuantity() {
            assertThatThrownBy(() -> new OrderItem(product, -1, new BigDecimal("25.00")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsNullQuantity() {
            assertThatThrownBy(() -> new OrderItem(product, null, new BigDecimal("25.00")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsZeroPrice() {
            assertThatThrownBy(() -> new OrderItem(product, 3, BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Price");
        }

        @Test
        void rejectsNegativePrice() {
            assertThatThrownBy(() -> new OrderItem(product, 3, new BigDecimal("-1")))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getSubtotal")
    class Subtotal {

        @Test
        void multipliesPriceByQuantity() {
            OrderItem item = new OrderItem(product, 4, new BigDecimal("25.00"));

            assertThat(item.getSubtotal()).isEqualByComparingTo("100.00");
        }

        @Test
        void handlesDecimalPrice() {
            OrderItem item = new OrderItem(product, 3, new BigDecimal("9.99"));

            assertThat(item.getSubtotal()).isEqualByComparingTo("29.97");
        }
    }
}
