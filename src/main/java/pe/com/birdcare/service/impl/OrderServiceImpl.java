package pe.com.birdcare.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.birdcare.dto.AdminOrderRequestDTO;
import pe.com.birdcare.dto.OrderItemRequestDTO;
import pe.com.birdcare.dto.OrderRequestDTO;
import pe.com.birdcare.dto.OrderResponseDTO;
import pe.com.birdcare.entity.Order;
import pe.com.birdcare.entity.OrderItem;
import pe.com.birdcare.entity.Product;
import pe.com.birdcare.entity.User;
import pe.com.birdcare.enums.OrderStatus;
import pe.com.birdcare.exception.BadRequestException;
import pe.com.birdcare.exception.ResourceNotFoundException;
import pe.com.birdcare.mapper.OrderMapper;
import pe.com.birdcare.repository.OrderRepository;
import pe.com.birdcare.repository.ProductRepository;
import pe.com.birdcare.repository.UserRepository;
import pe.com.birdcare.service.IOrderService;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderMapper mapper;

    @Override
    public OrderResponseDTO findById(Long id) {
        return mapper.toResponse(getOrderOrThrow(id));
    }

    @Override
    public Page<OrderResponseDTO> findByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId,pageable).map(mapper::toResponse);
    }

    //AUTHENTICATED USERS
    @Override
    public Page<OrderResponseDTO> findMyOrders(String email,Pageable pageable) {
        User existingUser=getUserByEmailOrThrow(email);
        return orderRepository.findByUserId(existingUser.getId(), pageable).map(mapper::toResponse);
    }

    @Transactional
    @Override
    public OrderResponseDTO createOrder(AdminOrderRequestDTO req) {
        User user = getUserOrThrow(req.userId());
        return orderCreation(user,req.shippingAddress(),req.items());
    }

    @Transactional
    @Override
    public OrderResponseDTO createMyOrder(String email, OrderRequestDTO req) {
        User user = getUserByEmailOrThrow(email);
        return orderCreation(user,req.shippingAddress(),req.items());
    }

    @Transactional
    @Override
    public OrderResponseDTO updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = getOrderOrThrow(orderId);

        if (newStatus == OrderStatus.CANCELLED) {
            handleCancellation(order);
        } else {
            order.setStatus(newStatus);
        }

        return mapper.toResponse(orderRepository.save(order));
    }

    private void handleCancellation(Order order) {
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel a delivered order.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.getItems().forEach(item -> {
            Product p = item.getProduct();
            p.setStock(p.getStock() + item.getQuantity());
        });
    }

    private OrderResponseDTO orderCreation(User user, String address, List<OrderItemRequestDTO> itemsDto){

        Order order = new Order(user,address);

        itemsDto.forEach(dto->{
            Product product = getProductOrThrow(dto.productId());
            if (product.getStock() < dto.quantity()) {
                throw new BadRequestException("Not enough stock for: " + product.getName());
            }

            product.setStock(product.getStock()-dto.quantity());

            OrderItem item = new OrderItem(product,dto.quantity(),product.getPrice());
            order.addItem(item);
        });

        return mapper.toResponse(orderRepository.save(order));
    }

    private Product getProductOrThrow(Long id){
        return productRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Product not found with ID: "+id));
    }

    private User getUserOrThrow(Long id){
        return userRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("User not found with ID: "+id));
    }

    private Order getOrderOrThrow(Long id){
        return orderRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Order not found with id: "+id));
    }

    private User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with email:"+email));
    }

}
