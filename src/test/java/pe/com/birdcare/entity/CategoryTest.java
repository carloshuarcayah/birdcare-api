package pe.com.birdcare.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryTest {

    private static final String NAME = "Alimento";
    private static final String DESCRIPTION = "Comida para aves";

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        void createsActiveCategory() {
            Category category = new Category(NAME, DESCRIPTION);

            assertThat(category.getName()).isEqualTo(NAME);
            assertThat(category.getDescription()).isEqualTo(DESCRIPTION);
            assertThat(category.getActive()).isTrue();
        }

        @Test
        void acceptsNullDescription() {
            Category category = new Category(NAME, null);
            assertThat(category.getDescription()).isNull();
        }

        @Test
        void rejectsBlankName() {
            assertThatThrownBy(() -> new Category("  ", DESCRIPTION))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name");
        }

        @Test
        void rejectsNullName() {
            assertThatThrownBy(() -> new Category(null, DESCRIPTION))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        void changesNameAndDescription() {
            Category category = new Category(NAME, DESCRIPTION);

            category.update("Juguetes", "Para distraerse");

            assertThat(category.getName()).isEqualTo("Juguetes");
            assertThat(category.getDescription()).isEqualTo("Para distraerse");
        }

        @Test
        void rejectsBlankNameKeepingState() {
            Category category = new Category(NAME, DESCRIPTION);

            assertThatThrownBy(() -> category.update(" ", "nueva"))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(category.getName()).isEqualTo(NAME);
            assertThat(category.getDescription()).isEqualTo(DESCRIPTION);
        }
    }

    @Nested
    @DisplayName("enable / disable")
    class EnableDisable {

        @Test
        void disableTurnsActiveFalse() {
            Category category = new Category(NAME, DESCRIPTION);

            category.disable();

            assertThat(category.getActive()).isFalse();
        }

        @Test
        void enableTurnsActiveTrue() {
            Category category = new Category(NAME, DESCRIPTION);
            category.disable();

            category.enable();

            assertThat(category.getActive()).isTrue();
        }
    }
}