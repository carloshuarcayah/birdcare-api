package pe.com.birdcare.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    private static final String NAME = "Semillas premium";
    private static final String DESCRIPTION = "Mix de semillas para canarios";
    private static final BigDecimal PRICE = new BigDecimal("25.50");
    private static final Integer STOCK = 10;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category("Alimento", "Comida para aves");
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        void createsActiveProduct() {
            Product product = Product.create(NAME, DESCRIPTION, PRICE, STOCK, category);

            assertThat(product.getName()).isEqualTo(NAME);
            assertThat(product.getDescription()).isEqualTo(DESCRIPTION);
            assertThat(product.getPrice()).isEqualByComparingTo(PRICE);
            assertThat(product.getStock()).isEqualTo(STOCK);
            assertThat(product.getCategory()).isEqualTo(category);
            assertThat(product.getActive()).isTrue();
        }

        @Test
        void usesDefaultDescriptionWhenNull() {
            Product product = Product.create(NAME, null, PRICE, STOCK, category);
            assertThat(product.getDescription()).isNotBlank();
        }

        @Test
        void rejectsBlankName() {
            assertThatThrownBy(() -> Product.create(" ", DESCRIPTION, PRICE, STOCK, category))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name");
        }

        @Test
        void rejectsNegativePrice() {
            assertThatThrownBy(() -> Product.create(NAME, DESCRIPTION, new BigDecimal("-1"), STOCK, category))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Price");
        }

        @Test
        void allowsZeroPrice() {
            Product product = Product.create(NAME, DESCRIPTION, BigDecimal.ZERO, STOCK, category);
            assertThat(product.getPrice()).isEqualByComparingTo("0");
        }

        @Test
        void rejectsNegativeStock() {
            assertThatThrownBy(() -> Product.create(NAME, DESCRIPTION, PRICE, -1, category))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stock");
        }

        @Test
        void rejectsNullCategory() {
            assertThatThrownBy(() -> Product.create(NAME, DESCRIPTION, PRICE, STOCK, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Category");
        }
    }

    @Nested
    @DisplayName("updateDetails")
    class UpdateDetails {

        @Test
        void updatesAllFields() {
            Product product = Product.create(NAME, DESCRIPTION, PRICE, STOCK, category);
            Category newCategory = new Category("Juguetes", null);

            product.updateDetails("Nuevo", "Nueva desc", new BigDecimal("30"), newCategory);

            assertThat(product.getName()).isEqualTo("Nuevo");
            assertThat(product.getDescription()).isEqualTo("Nueva desc");
            assertThat(product.getPrice()).isEqualByComparingTo("30");
            assertThat(product.getCategory()).isEqualTo(newCategory);
        }

        @Test
        void doesNotChangeStockOrActive() {
            Product product = Product.create(NAME, DESCRIPTION, PRICE, STOCK, category);

            product.updateDetails("Nuevo", "desc", new BigDecimal("30"), category);

            assertThat(product.getStock()).isEqualTo(STOCK);
            assertThat(product.getActive()).isTrue();
        }

        @Test
        void rejectsBlankNameKeepingState() {
            Product product = Product.create(NAME, DESCRIPTION, PRICE, STOCK, category);

            assertThatThrownBy(() -> product.updateDetails(" ", "d", new BigDecimal("30"), category))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(product.getName()).isEqualTo(NAME);
        }
    }

    @Nested
    @DisplayName("enable / disable")
    class EnableDisable {

        @Test
        void disableTurnsActiveFalse() {
            Product product = Product.create(NAME, DESCRIPTION, PRICE, STOCK, category);

            product.disable();

            assertThat(product.getActive()).isFalse();
        }

        @Test
        void enableTurnsActiveTrue() {
            Product product = Product.create(NAME, DESCRIPTION, PRICE, STOCK, category);
            product.disable();

            product.enable();

            assertThat(product.getActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("decreaseStock")
    class DecreaseStock {

        @Test
        void reducesStockByQuantity() {
            Product product = Product.create(NAME, DESCRIPTION, PRICE, 10, category);

            product.decreaseStock(3);

            assertThat(product.getStock()).isEqualTo(7);
        }

        @Test
        void allowsReducingToZero() {
            Product product = Product.create(NAME, DESCRIPTION, PRICE, 5, category);

            product.decreaseStock(5);

            assertThat(product.getStock()).isZero();
        }

        @Test
        void rejectsMoreThanAvailable() {
            Product product = Product.create(NAME, DESCRIPTION, PRICE, 5, category);

            assertThatThrownBy(() -> product.decreaseStock(6))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining(NAME);

            assertThat(product.getStock()).isEqualTo(5);
        }

        @Test
        void rejectsZeroOrNegativeQuantity() {
            Product product = Product.create(NAME, DESCRIPTION, PRICE, 5, category);

            assertThatThrownBy(() -> product.decreaseStock(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> product.decreaseStock(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("restoreStock")
    class RestoreStock {

        @Test
        void addsToStock() {
            Product product = Product.create(NAME, DESCRIPTION, PRICE, 5, category);

            product.restoreStock(3);

            assertThat(product.getStock()).isEqualTo(8);
        }

        @Test
        void rejectsZeroOrNegativeQuantity() {
            Product product = Product.create(NAME, DESCRIPTION, PRICE, 5, category);

            assertThatThrownBy(() -> product.restoreStock(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> product.restoreStock(-2))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
