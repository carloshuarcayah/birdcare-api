package pe.com.birdcare.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pe.com.birdcare.enums.OrderStatus;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final String ADDRESS = "Av. Siempre Viva 742";

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.registerCustomer("carlos@test.com", "enc:x", "Carlos", "Huarcaya", "999");
        Category category = new Category("Alimento", "Comida");
        product = Product.create("Semillas", "Mix", new BigDecimal("25.00"), 100, category);
    }

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        void startsPendingWithZeroTotalAndEmptyItems() {
            Order order = new Order(user, ADDRESS);

            assertThat(order.getUser()).isEqualTo(user);
            assertThat(order.getShippingAddress()).isEqualTo(ADDRESS);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getTotal()).isEqualByComparingTo("0");
            assertThat(order.getItems()).isEmpty();
        }

        @Test
        void rejectsNullUser() {
            assertThatThrownBy(() -> new Order(null, ADDRESS))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("user");
        }

        @Test
        void rejectsBlankAddress() {
            assertThatThrownBy(() -> new Order(user, "  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Shipping");
        }

        @Test
        void rejectsNullAddress() {
            assertThatThrownBy(() -> new Order(user, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("addItem")
    class AddItem {

        @Test
        void appendsItemAndSetsBackReference() {
            Order order = new Order(user, ADDRESS);
            OrderItem item = new OrderItem(product, 2, new BigDecimal("25.00"));

            order.addItem(item);

            assertThat(order.getItems()).containsExactly(item);
            assertThat(item.getOrder()).isSameAs(order);
        }

        @Test
        void updatesTotalForSingleItem() {
            Order order = new Order(user, ADDRESS);

            order.addItem(new OrderItem(product, 3, new BigDecimal("25.00")));

            assertThat(order.getTotal()).isEqualByComparingTo("75.00");
        }

        @Test
        void accumulatesTotalAcrossItems() {
            Order order = new Order(user, ADDRESS);

            order.addItem(new OrderItem(product, 2, new BigDecimal("25.00")));  // 50
            order.addItem(new OrderItem(product, 1, new BigDecimal("9.99")));   // 9.99
            order.addItem(new OrderItem(product, 4, new BigDecimal("5.50")));   // 22.00

            assertThat(order.getTotal()).isEqualByComparingTo("81.99");
            assertThat(order.getItems()).hasSize(3);
        }
    }
}
