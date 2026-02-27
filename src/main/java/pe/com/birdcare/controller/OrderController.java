package pe.com.birdcare.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.com.birdcare.dto.AdminOrderRequestDTO;
import pe.com.birdcare.dto.OrderRequestDTO;
import pe.com.birdcare.dto.OrderResponseDTO;
import pe.com.birdcare.enums.OrderStatus;
import pe.com.birdcare.service.IOrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;

    @GetMapping("/me")
    public ResponseEntity<Page<OrderResponseDTO>> getMyOrders(@AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {
        return ResponseEntity.ok(orderService.findMyOrders(userDetails.getUsername(), pageable));
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponseDTO> create(@AuthenticationPrincipal UserDetails userDetails,@Valid @RequestBody OrderRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createMyOrder(userDetails.getUsername(),req));
    }
}
