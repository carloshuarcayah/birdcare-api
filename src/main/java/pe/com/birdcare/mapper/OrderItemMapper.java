package pe.com.birdcare.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.com.birdcare.dto.OrderItemResponseDTO;
import pe.com.birdcare.entity.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(expression = "java(orderItem.getSubtotal())", target = "subtotal")
    OrderItemResponseDTO toResponse(OrderItem orderItem);
}
