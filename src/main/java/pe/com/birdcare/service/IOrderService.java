package pe.com.birdcare.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.com.birdcare.dto.AdminOrderRequestDTO;
import pe.com.birdcare.dto.OrderRequestDTO;
import pe.com.birdcare.dto.OrderResponseDTO;
import pe.com.birdcare.enums.OrderStatus;

public interface IOrderService {
    OrderResponseDTO findById(Long id); //ADMIN
    Page<OrderResponseDTO> findByUserId(Long userId, Pageable pageable); //ADMIN
    Page<OrderResponseDTO> findMyOrders(Pageable pageable);
    OrderResponseDTO createOrder(AdminOrderRequestDTO req); //ADMIN
    OrderResponseDTO createMyOrder(OrderRequestDTO req); //User
    OrderResponseDTO updateStatus(Long orderId, OrderStatus orderStatus); //ADMIN
}
