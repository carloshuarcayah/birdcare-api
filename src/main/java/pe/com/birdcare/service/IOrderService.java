package pe.com.birdcare.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.com.birdcare.dto.OrderRequestDTO;
import pe.com.birdcare.dto.OrderResponseDTO;
import pe.com.birdcare.enums.OrderStatus;

public interface IOrderService {
    OrderResponseDTO findById(Long id);
    Page<OrderResponseDTO> findByUserId(Long userId, Pageable pageable);
    OrderResponseDTO create(OrderRequestDTO req);
    OrderResponseDTO update(Long orderId, OrderStatus orderStatus);
}
