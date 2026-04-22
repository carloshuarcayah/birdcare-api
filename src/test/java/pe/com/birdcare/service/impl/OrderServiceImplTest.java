package pe.com.birdcare.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pe.com.birdcare.dto.AdminOrderRequestDTO;
import pe.com.birdcare.dto.OrderItemRequestDTO;
import pe.com.birdcare.dto.OrderRequestDTO;
import pe.com.birdcare.dto.OrderResponseDTO;
import pe.com.birdcare.entity.*;
import pe.com.birdcare.enums.OrderStatus;
import pe.com.birdcare.exception.BadRequestException;
import pe.com.birdcare.exception.ResourceNotFoundException;
import pe.com.birdcare.mapper.OrderMapper;
import pe.com.birdcare.repository.OrderRepository;
import pe.com.birdcare.repository.ProductRepository;
import pe.com.birdcare.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final Long ORDER_ID = 1L;
    private static final Long USER_ID = 10L;
    private static final Long PRODUCT_ID = 100L;
    private static final String EMAIL = "carlos@test.com";
    private static final String ADDRESS = "Av. Siempre Viva 742";

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderMapper mapper;

    @InjectMocks private OrderServiceImpl service;

    private User user;
    private Product product;
    private Order existingOrder;
    private OrderResponseDTO responseDto;

    @BeforeEach
    void setUp() {
        user = User.registerCustomer(EMAIL, "enc:x", "Carlos", "Huarcaya", "999");
        Category category = new Category("Alimento", "Comida");
        product = Product.create("Semillas", "Mix", new BigDecimal("25.00"), 10, category);
        existingOrder = new Order(user, ADDRESS);
        responseDto = OrderResponseDTO.builder()
                .id(ORDER_ID)
                .userId(USER_ID)
                .total(BigDecimal.ZERO)
                .status("PENDING")
                .shippingAddress(ADDRESS)
                .items(List.of())
                .build();
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        void returnsDtoWhenFound() {
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(existingOrder));
            when(mapper.toResponse(existingOrder)).thenReturn(responseDto);

            assertThat(service.findById(ORDER_ID)).isEqualTo(responseDto);
        }

        @Test
        void throwsWhenNotFound() {
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(ORDER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findByUserId / findMyOrders")
    class FindQueries {

        private final Pageable pageable = PageRequest.of(0, 10);

        @Test
        void findByUserIdDelegates() {
            when(orderRepository.findByUserId(USER_ID, pageable))
                    .thenReturn(new PageImpl<>(List.of(existingOrder)));
            when(mapper.toResponse(existingOrder)).thenReturn(responseDto);

            service.findByUserId(USER_ID, pageable);

            verify(orderRepository).findByUserId(USER_ID, pageable);
        }

        @Test
        void findMyOrdersLooksUpUserByEmail() {
            user = spy(user);
            when(user.getId()).thenReturn(USER_ID);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(orderRepository.findByUserId(USER_ID, pageable))
                    .thenReturn(new PageImpl<>(List.of(existingOrder)));
            when(mapper.toResponse(existingOrder)).thenReturn(responseDto);

            service.findMyOrders(EMAIL, pageable);

            verify(orderRepository).findByUserId(USER_ID, pageable);
        }

        @Test
        void findMyOrdersThrowsWhenUserMissing() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findMyOrders(EMAIL, pageable))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createOrder (admin)")
    class CreateAdmin {

        private final AdminOrderRequestDTO dto = new AdminOrderRequestDTO(
                USER_ID, ADDRESS,
                List.of(new OrderItemRequestDTO(PRODUCT_ID, 3)));

        @Test
        void resolvesUserDecreasesStockAndSavesOrder() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.toResponse(any(Order.class))).thenReturn(responseDto);

            service.createOrder(dto);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            Order saved = captor.getValue();

            assertThat(saved.getUser()).isEqualTo(user);
            assertThat(saved.getShippingAddress()).isEqualTo(ADDRESS);
            assertThat(saved.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(saved.getItems()).hasSize(1);
            assertThat(saved.getTotal()).isEqualByComparingTo("75.00");
            assertThat(product.getStock()).isEqualTo(7);
        }

        @Test
        void throwsWhenUserNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createOrder(dto))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(orderRepository, never()).save(any());
            assertThat(product.getStock()).isEqualTo(10);
        }

        @Test
        void throwsWhenProductNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createOrder(dto))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(orderRepository, never()).save(any());
        }

        @Test
        void throwsWhenInsufficientStock() {
            AdminOrderRequestDTO tooMuch = new AdminOrderRequestDTO(
                    USER_ID, ADDRESS,
                    List.of(new OrderItemRequestDTO(PRODUCT_ID, 999)));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

            assertThatThrownBy(() -> service.createOrder(tooMuch))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("stock");

            verify(orderRepository, never()).save(any());
            assertThat(product.getStock()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("createMyOrder")
    class CreateMyOrder {

        private final OrderRequestDTO dto = new OrderRequestDTO(
                ADDRESS, List.of(new OrderItemRequestDTO(PRODUCT_ID, 2)));

        @Test
        void looksUpUserByEmailAndCreatesOrder() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.toResponse(any(Order.class))).thenReturn(responseDto);

            service.createMyOrder(EMAIL, dto);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());

            assertThat(captor.getValue().getUser()).isEqualTo(user);
            assertThat(captor.getValue().getTotal()).isEqualByComparingTo("50.00");
            assertThat(product.getStock()).isEqualTo(8);
        }

        @Test
        void throwsWhenEmailNotFound() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createMyOrder(EMAIL, dto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatus {

        @Test
        void setsStatusForNonCancelTransition() {
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(existingOrder));
            when(orderRepository.save(existingOrder)).thenReturn(existingOrder);
            when(mapper.toResponse(existingOrder)).thenReturn(responseDto);

            service.updateStatus(ORDER_ID, OrderStatus.SHIPPED);

            assertThat(existingOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);
            verify(orderRepository).save(existingOrder);
        }

        @Test
        void cancelRestoresStockOnAllItems() {
            existingOrder.addItem(new OrderItem(product, 3, new BigDecimal("25.00")));
            product.decreaseStock(3);
            assertThat(product.getStock()).isEqualTo(7);

            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(existingOrder));
            when(orderRepository.save(existingOrder)).thenReturn(existingOrder);
            when(mapper.toResponse(existingOrder)).thenReturn(responseDto);

            service.updateStatus(ORDER_ID, OrderStatus.CANCELLED);

            assertThat(existingOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(product.getStock()).isEqualTo(10);
        }

        @Test
        void cancelFailsForDeliveredOrder() {
            existingOrder.setStatus(OrderStatus.DELIVERED);
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(existingOrder));

            assertThatThrownBy(() -> service.updateStatus(ORDER_ID, OrderStatus.CANCELLED))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("delivered");

            assertThat(existingOrder.getStatus()).isEqualTo(OrderStatus.DELIVERED);
            verify(orderRepository, never()).save(any());
        }

        @Test
        void throwsWhenOrderNotFound() {
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateStatus(ORDER_ID, OrderStatus.SHIPPED))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
