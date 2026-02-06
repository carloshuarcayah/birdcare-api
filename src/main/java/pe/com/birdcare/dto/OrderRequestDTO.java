package pe.com.birdcare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequestDTO(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotBlank(message = "Shipping address is required")
        String shippingAddress,

        @NotEmpty(message = "The order must have at least one item")
        List<OrderItemRequestDTO> items
) {
}
