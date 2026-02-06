package pe.com.birdcare.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderItemResponseDTO(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal price,
        BigDecimal subtotal
) {
}
