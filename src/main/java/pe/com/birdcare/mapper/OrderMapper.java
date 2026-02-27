package pe.com.birdcare.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.com.birdcare.dto.OrderResponseDTO;
import pe.com.birdcare.entity.Order;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(expression = "java(order.getUser().getName() + ' ' + order.getUser().getLastName())", target = "userName")
    OrderResponseDTO toResponse(Order order);
}