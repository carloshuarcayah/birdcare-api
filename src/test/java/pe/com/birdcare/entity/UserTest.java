package pe.com.birdcare.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.com.birdcare.enums.Role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    private static final String EMAIL = "carlos@test.com";
    private static final String RAW_PASSWORD = "Secret123";
    private static final String NAME = "Carlos";
    private static final String LAST_NAME = "Huarcaya";
    private static final String PHONE = "999999999";

    private final PasswordEncoder encoder = new FakeEncoder();

    @Nested
    @DisplayName("registerCustomer")
    class RegisterCustomer {

        @Test
        void createsUserWithCustomerRoleAndActive() {
            User user = User.registerCustomer(EMAIL, RAW_PASSWORD, NAME, LAST_NAME, PHONE);

            assertThat(user.getEmail()).isEqualTo(EMAIL);
            assertThat(user.getPassword()).isEqualTo(RAW_PASSWORD);
            assertThat(user.getName()).isEqualTo(NAME);
            assertThat(user.getLastName()).isEqualTo(LAST_NAME);
            assertThat(user.getPhone()).isEqualTo(PHONE);
            assertThat(user.getRole()).isEqualTo(Role.CUSTOMER);
            assertThat(user.getActive()).isTrue();
        }

        @Test
        void acceptsNullPhone() {
            User user = User.registerCustomer(EMAIL, RAW_PASSWORD, NAME, LAST_NAME, null);
            assertThat(user.getPhone()).isNull();
        }

        @Test
        void rejectsBlankName() {
            assertThatThrownBy(() -> User.registerCustomer(EMAIL, RAW_PASSWORD, "  ", LAST_NAME, PHONE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Name");
        }

        @Test
        void rejectsBlankLastName() {
            assertThatThrownBy(() -> User.registerCustomer(EMAIL, RAW_PASSWORD, NAME, "", PHONE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Last name");
        }

        @Test
        void rejectsNullEmail() {
            assertThatThrownBy(() -> User.registerCustomer(null, RAW_PASSWORD, NAME, LAST_NAME, PHONE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("email");
        }
    }

    @Nested
    @DisplayName("createAdmin")
    class CreateAdmin {

        @Test
        void createsUserWithAdminRoleAndActive() {
            User user = User.createAdmin(EMAIL, RAW_PASSWORD, NAME, LAST_NAME, PHONE);

            assertThat(user.getRole()).isEqualTo(Role.ADMIN);
            assertThat(user.getActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("enable / disable")
    class EnableDisable {

        @Test
        void disableTurnsActiveFalse() {
            User user = User.registerCustomer(EMAIL, RAW_PASSWORD, NAME, LAST_NAME, PHONE);

            user.disable();

            assertThat(user.getActive()).isFalse();
        }

        @Test
        void enableTurnsActiveTrue() {
            User user = User.registerCustomer(EMAIL, RAW_PASSWORD, NAME, LAST_NAME, PHONE);
            user.disable();

            user.enable();

            assertThat(user.getActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("updateInfo")
    class UpdateInfo {

        @Test
        void updatesNameLastNameAndPhone() {
            User user = User.registerCustomer(EMAIL, RAW_PASSWORD, NAME, LAST_NAME, PHONE);

            user.updateInfo("Pedro", "Perez", "111222333");

            assertThat(user.getName()).isEqualTo("Pedro");
            assertThat(user.getLastName()).isEqualTo("Perez");
            assertThat(user.getPhone()).isEqualTo("111222333");
        }

        @Test
        void doesNotChangeEmailOrRole() {
            User user = User.registerCustomer(EMAIL, RAW_PASSWORD, NAME, LAST_NAME, PHONE);

            user.updateInfo("Pedro", "Perez", "111");

            assertThat(user.getEmail()).isEqualTo(EMAIL);
            assertThat(user.getRole()).isEqualTo(Role.CUSTOMER);
        }

        @Test
        void rejectsBlankName() {
            User user = User.registerCustomer(EMAIL, RAW_PASSWORD, NAME, LAST_NAME, PHONE);

            assertThatThrownBy(() -> user.updateInfo(" ", "Perez", "111"))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(user.getName()).isEqualTo(NAME);
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        void encodesAndStoresNewPassword() {
            User user = userWithEncodedPassword();

            user.changePassword(RAW_PASSWORD, "BrandNew1", encoder);

            assertThat(user.getPassword()).isEqualTo("enc:BrandNew1");
        }

        @Test
        void rejectsWrongCurrentPassword() {
            User user = userWithEncodedPassword();

            assertThatThrownBy(() -> user.changePassword("wrong", "BrandNew1", encoder))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Current password");

            assertThat(user.getPassword()).isEqualTo("enc:" + RAW_PASSWORD);
        }

        @Test
        void rejectsNullCurrentPassword() {
            User user = userWithEncodedPassword();

            assertThatThrownBy(() -> user.changePassword(null, "BrandNew1", encoder))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsSameNewPassword() {
            User user = userWithEncodedPassword();

            assertThatThrownBy(() -> user.changePassword(RAW_PASSWORD, RAW_PASSWORD, encoder))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("same");

            assertThat(user.getPassword()).isEqualTo("enc:" + RAW_PASSWORD);
        }
    }

    @Nested
    @DisplayName("resetPassword")
    class ResetPassword {

        @Test
        void encodesAndStoresNewPassword() {
            User user = userWithEncodedPassword();

            user.resetPassword("AdminSet1", encoder);

            assertThat(user.getPassword()).isEqualTo("enc:AdminSet1");
        }

        @Test
        void rejectsBlankPassword() {
            User user = userWithEncodedPassword();

            assertThatThrownBy(() -> user.resetPassword("  ", encoder))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    private User userWithEncodedPassword() {
        return User.registerCustomer(EMAIL, encoder.encode(RAW_PASSWORD), NAME, LAST_NAME, PHONE);
    }

    private static class FakeEncoder implements PasswordEncoder {
        @Override
        public String encode(CharSequence rawPassword) {
            return "enc:" + rawPassword;
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return encodedPassword.equals("enc:" + rawPassword);
        }
    }
}