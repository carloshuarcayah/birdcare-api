package pe.com.birdcare.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
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

import java.math.BigDecimal;
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
    public Page<OrderResponseDTO> findMyOrders(Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        User existingUser=userRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("User not found with email. "+email));

        return orderRepository.findByUserId(existingUser.getId(), pageable).map(mapper::toResponse);
    }

    @Transactional
    @Override
    public OrderResponseDTO createOrder(AdminOrderRequestDTO req) {
        User existingUser = getUserOrThrow(req.userId());
        Order newOrder = mapper.toEntity(req);
        newOrder.setUser(existingUser);
        return mapper.toResponse(prepareOrderAndSave(newOrder,req.items()));
    }

    @Transactional
    @Override
    public OrderResponseDTO createMyOrder(OrderRequestDTO req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User existingUser = userRepository.findByEmail(auth.getName()).orElseThrow(()->new ResourceNotFoundException("User not found"));
        Order newOrder = mapper.toEntity(req);
        newOrder.setUser(existingUser);

        return mapper.toResponse(prepareOrderAndSave(newOrder, req.items()));
    }

    @Transactional
    @Override
    public OrderResponseDTO updateStatus(Long orderId, OrderStatus orderStatus) {
        Order order = getOrderOrThrow(orderId);

        if (order.getStatus() == OrderStatus.DELIVERED && orderStatus == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel an order that has already been delivered.");
        }

        order.setStatus(orderStatus);

        if (orderStatus == OrderStatus.CANCELLED) {
            order.getItems().forEach(item -> {
                int currentStock = item.getProduct().getStock();
                item.getProduct().setStock(currentStock + item.getQuantity());
            });
        }

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

    private BigDecimal getSubtotal(BigDecimal price, Integer quantity){
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    private Order prepareOrderAndSave(Order order, List<OrderItemRequestDTO> req){

        //LOGIC FOR
        List<OrderItem> orderItems = req.stream().map(
                itemDTO->{
                    Product product = getProductOrThrow(itemDTO.productId());

                    if (product.getStock() < itemDTO.quantity()) {
                        throw new BadRequestException("Not enough stock for: " + product.getName());
                    }

                    product.setStock(product.getStock() - itemDTO.quantity());

                    return OrderItem.builder()
                            .order(order)
                            .product(product)
                            .quantity(itemDTO.quantity())
                            .price(product.getPrice()).build();
                }).toList();

        order.setItems(orderItems);

        BigDecimal total = orderItems.stream().map(item->getSubtotal(item.getPrice(), item.getQuantity())).reduce(BigDecimal.ZERO,BigDecimal::add);

        order.setTotal(total);

        return orderRepository.save(order);
    }
}
