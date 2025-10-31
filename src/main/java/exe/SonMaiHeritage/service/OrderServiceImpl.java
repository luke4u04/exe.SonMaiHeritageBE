package exe.SonMaiHeritage.service;

import exe.SonMaiHeritage.controller.OrderController;
import exe.SonMaiHeritage.entity.Order;
import exe.SonMaiHeritage.entity.OrderItem;
import exe.SonMaiHeritage.entity.User;
import exe.SonMaiHeritage.model.CheckoutRequest;
import exe.SonMaiHeritage.model.OrderResponse;
import exe.SonMaiHeritage.repository.OrderRepository;
import exe.SonMaiHeritage.repository.UserRepository;
import exe.SonMaiHeritage.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final EmailService emailService;
    
    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository, ProductService productService, EmailService emailService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productService = productService;
        this.emailService = emailService;
    }
    
    @Override
    public Order createOrder(CheckoutRequest checkoutRequest) {
        log.info("Creating order for user: {}", checkoutRequest.getUserId());
        
        String orderCode = "ORD" + System.currentTimeMillis();
        
        List<OrderItem> orderItems = checkoutRequest.getItems().stream()
                .map(item -> OrderItem.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .productPrice(item.getProductPrice())
                        .quantity(item.getQuantity())
                        .totalPrice(item.getProductPrice() * item.getQuantity())
                        .productImage(item.getProductImage())
                        .build())
                .collect(Collectors.toList());
        
        // Handle guest checkout (userId can be null)
        User user = null;
        if (checkoutRequest.getUserId() != null) {
            user = userRepository.findById(checkoutRequest.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + checkoutRequest.getUserId()));
        }

        Order order = Order.builder()
                .orderCode(orderCode)
                .user(user)
                .totalAmount(checkoutRequest.getTotalAmount())
                .status(Order.OrderStatus.PENDING)
                .paymentMethod("DIRECT")
                .paymentStatus("PENDING")
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .orderItems(orderItems)
                .shipFullName(checkoutRequest.getShipFullName())
                .shipPhone(checkoutRequest.getShipPhone())
                .shipEmail(checkoutRequest.getShipEmail())
                .shipStreet(checkoutRequest.getShipStreet())
                .shipWard(checkoutRequest.getShipWard())
                .shipDistrict(checkoutRequest.getShipDistrict())
                .shipProvince(checkoutRequest.getShipProvince())
                .note(checkoutRequest.getNote())
                .build();
        
        // Set order reference in order items
        orderItems.forEach(item -> item.setOrder(order));
        
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with code: {}", savedOrder.getOrderCode());
        
        // Send confirmation email
        try {
            emailService.sendSimpleOrderConfirmationEmail(savedOrder);
            log.info("Order confirmation email sent successfully for order: {}", savedOrder.getOrderCode());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order: {}, error: {}", 
                savedOrder.getOrderCode(), e.getMessage());
            // Don't fail the order creation if email fails
        }
        
        return savedOrder;
    }
    
    @Override
    public Order getOrderByCode(String orderCode) {
        log.info("Fetching order by code: {}", orderCode);
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Order not found with code: " + orderCode));
    }
    
    @Override
    public void updateOrderStatus(String orderCode, Order.OrderStatus status) {
        log.info("Updating order status for code: {} to {}", orderCode, status);
        Order order = getOrderByCode(orderCode);
        order.setStatus(status);
        order.setUpdatedDate(LocalDateTime.now());
        orderRepository.save(order);
        log.info("Order status updated successfully");
    }
    
    @Override
    public Page<Order> getAllOrders(Pageable pageable) {
        log.info("Retrieving all orders with pagination");
        return orderRepository.findAll(pageable);
    }
    
    @Override
    public Order getOrderById(Integer orderId) {
        log.info("Retrieving order by ID: {}", orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }
    
    @Override
    public List<Order> getOrdersByUserId(Integer userId) {
        log.info("Retrieving orders for user ID: {}", userId);
        return orderRepository.findByUserId(userId);
    }
    
    @Override
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        log.info("Retrieving orders by status: {}", status);
        return orderRepository.findByStatus(status);
    }
    
    @Override
    public List<Order> getPaidOrders() {
        log.info("Retrieving paid orders");
        return orderRepository.findPaidOrders();
    }
    
    @Override
    public List<Order> getGuestOrders() {
        log.info("Retrieving guest orders");
        return orderRepository.findByUserIdIsNull();
    }
    
    @Override
    public Page<Order> getGuestOrders(Pageable pageable) {
        log.info("Retrieving guest orders with pagination");
        return orderRepository.findByUserIdIsNull(pageable);
    }
    
    @Override
    public Order updateOrderStatus(Integer orderId, Order.OrderStatus status) {
        log.info("Updating order {} status to {}", orderId, status);
        Order order = getOrderById(orderId);
        order.setStatus(status);
        order.setUpdatedDate(LocalDateTime.now());
        return orderRepository.save(order);
    }
    
    @Override
    public OrderController.OrderStatistics getOrderStatistics() {
        log.info("Calculating order statistics");
        
        long totalOrders = orderRepository.count();
        long paidOrders = orderRepository.findPaidOrders().size();
        long pendingOrders = orderRepository.countByStatus(Order.OrderStatus.PENDING);
        long cancelledOrders = orderRepository.countByStatus(Order.OrderStatus.CANCELLED);
        
        Long totalRevenue = orderRepository.sumTotalRevenue();
        
        return new OrderController.OrderStatistics(
                totalOrders,
                paidOrders,
                pendingOrders,
                cancelledOrders,
                totalRevenue != null ? totalRevenue : 0L
        );
    }

    @Override
    public void processOrderPayment(String orderCode) {
        log.info("Processing payment for order: {}", orderCode);
        Order order = getOrderByCode(orderCode);
        
        // Update order status to CONFIRMED
        order.setStatus(Order.OrderStatus.CONFIRMED);
        order.setPaymentStatus("SUCCESS");
        order.setUpdatedDate(LocalDateTime.now());
        
        // Subtract product quantities
        for (OrderItem item : order.getOrderItems()) {
            try {
                productService.updateProductQuantity(item.getProductId(), item.getQuantity());
                log.info("Updated quantity for product ID: {}, subtracted: {}", item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                log.error("Failed to update quantity for product ID: {}, error: {}", item.getProductId(), e.getMessage());
                // You might want to handle this differently based on business requirements
                throw new RuntimeException("Failed to update product inventory: " + e.getMessage());
            }
        }
        
        orderRepository.save(order);
        log.info("Order payment processed successfully: {}", orderCode);
    }

    @Override
    public OrderResponse convertToOrderResponse(Order order) {
        List<OrderResponse.OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .productPrice(item.getProductPrice())
                        .quantity(item.getQuantity())
                        .totalPrice(item.getTotalPrice())
                        .productImage(item.getProductImage())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .userFullName(order.getUser() != null ? order.getUser().getFullName() : null)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .createdDate(order.getCreatedDate())
                .updatedDate(order.getUpdatedDate())
                .shipFullName(order.getShipFullName())
                .shipPhone(order.getShipPhone())
                .shipStreet(order.getShipStreet())
                .shipWard(order.getShipWard())
                .shipDistrict(order.getShipDistrict())
                .shipProvince(order.getShipProvince())
                .orderItems(orderItemResponses)
                .build();
    }

    @Override
    public List<OrderResponse> convertToOrderResponseList(List<Order> orders) {
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }
}
