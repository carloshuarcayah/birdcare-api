package pe.com.birdcare.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.com.birdcare.dto.OrderRequestDTO;
import pe.com.birdcare.dto.OrderResponseDTO;
import pe.com.birdcare.enums.OrderStatus;

public interface IOrderService {
    OrderResponseDTO findById(Long id);
    Page<OrderResponseDTO> findByUserId(Long userId, Pageable pageable);
    Page<OrderResponseDTO> findMyOrders(Pageable pageable);
    OrderResponseDTO create(OrderRequestDTO req);
    OrderResponseDTO updateStatus(Long orderId, OrderStatus orderStatus);
}
