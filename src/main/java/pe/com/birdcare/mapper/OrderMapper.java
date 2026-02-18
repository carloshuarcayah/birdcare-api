package pe.com.birdcare.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.birdcare.dto.AdminOrderRequestDTO;
import pe.com.birdcare.dto.OrderItemResponseDTO;
import pe.com.birdcare.dto.OrderRequestDTO;
import pe.com.birdcare.dto.OrderResponseDTO;
import pe.com.birdcare.entity.Order;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final OrderItemMapper orderItemMapper;

    //create REQ->ENT
    public  Order toEntity(OrderRequestDTO req){
        return Order.builder()
                .shippingAddress(req.shippingAddress())
                .build();
    }

    public  Order toEntity(AdminOrderRequestDTO req){
        return Order.builder()
                .shippingAddress(req.shippingAddress())
                .build();
    }

    public  OrderResponseDTO toResponse(Order order){
        //The user must exist in the database

        Long userId = order.getUser().getId();
        String userName = order.getUser().getName()+" "+order.getUser().getLastName();

        List<OrderItemResponseDTO> items = order.getItems().stream().map(orderItemMapper::toResponse).toList();

        return OrderResponseDTO.builder()
                .id(order.getId())
                .userId(userId)
                .userName(userName)
                .orderDate(order.getOrderDate())
                .total(order.getTotal())
                .status(order.getStatus().name())
                .shippingAddress(order.getShippingAddress())
                .items(items)
                .build();
    }
}
