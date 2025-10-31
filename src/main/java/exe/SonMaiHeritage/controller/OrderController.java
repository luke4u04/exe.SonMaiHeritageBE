package exe.SonMaiHeritage.controller;

import exe.SonMaiHeritage.entity.Order;
import exe.SonMaiHeritage.model.OrderResponse;
import exe.SonMaiHeritage.service.OrderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing orders and order history
 */
@RestController
@RequestMapping("/api/orders")
@Log4j2
public class OrderController {
    
    private final OrderService orderService;
    
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    /**
     * Get all orders with pagination and sorting
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<Order> orders = orderService.getAllOrders(pageable);
            
            // Convert to OrderResponse to avoid circular reference
            List<OrderResponse> orderResponses = orderService.convertToOrderResponseList(orders.getContent());
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", orderResponses);
            response.put("totalElements", orders.getTotalElements());
            response.put("totalPages", orders.getTotalPages());
            response.put("size", orders.getSize());
            response.put("number", orders.getNumber());
            response.put("first", orders.isFirst());
            response.put("last", orders.isLast());
            
            log.info("Retrieved {} orders", orders.getTotalElements());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get orders by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable Integer userId) {
        try {
            List<Order> orders = orderService.getOrdersByUserId(userId);
            List<OrderResponse> orderResponses = orderService.convertToOrderResponseList(orders);
            log.info("Retrieved {} orders for user {}", orders.size(), userId);
            return ResponseEntity.ok(orderResponses);
            
        } catch (Exception e) {
            log.error("Error retrieving orders for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get orders by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String status) {
        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            List<Order> orders = orderService.getOrdersByStatus(orderStatus);
            
            log.info("Retrieved {} orders with status {}", orders.size(), status);
            return ResponseEntity.ok(orders);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid order status: {}", status);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error retrieving orders by status {}: {}", status, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get paid orders (CONFIRMED, SHIPPING, DELIVERED)
     */
    @GetMapping("/paid")
    public ResponseEntity<List<Order>> getPaidOrders() {
        try {
            List<Order> paidOrders = orderService.getPaidOrders();
            log.info("Retrieved {} paid orders", paidOrders.size());
            return ResponseEntity.ok(paidOrders);
            
        } catch (Exception e) {
            log.error("Error retrieving paid orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get order by order code
     */
    @GetMapping("/code/{orderCode}")
    public ResponseEntity<Order> getOrderByCode(@PathVariable String orderCode) {
        try {
            Order order = orderService.getOrderByCode(orderCode);
            log.info("Retrieved order with code: {}", orderCode);
            return ResponseEntity.ok(order);
            
        } catch (Exception e) {
            log.error("Error retrieving order with code {}: {}", orderCode, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Integer orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            log.info("Retrieved order with ID: {}", orderId);
            return ResponseEntity.ok(order);
            
        } catch (Exception e) {
            log.error("Error retrieving order with ID {}: {}", orderId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Update order status
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Integer orderId,
            @RequestParam String status) {
        
        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            Order updatedOrder = orderService.updateOrderStatus(orderId, orderStatus);
            
            log.info("Updated order {} status to {}", orderId, status);
            return ResponseEntity.ok(updatedOrder);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid order status: {}", status);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating order {} status: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get order statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<OrderStatistics> getOrderStatistics() {
        try {
            OrderStatistics stats = orderService.getOrderStatistics();
            log.info("Retrieved order statistics");
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error retrieving order statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all orders with detailed information (for admin)
     */
    @GetMapping("/all-detailed")
    public ResponseEntity<List<OrderResponse>> getAllOrdersDetailed() {
        try {
            List<Order> orders = orderService.getAllOrders(Pageable.unpaged()).getContent();
            List<OrderResponse> orderResponses = orderService.convertToOrderResponseList(orders);
            log.info("Retrieved {} detailed orders", orders.size());
            return ResponseEntity.ok(orderResponses);
            
        } catch (Exception e) {
            log.error("Error retrieving detailed orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get guest orders only (for admin)
     */
    @GetMapping("/guest")
    public ResponseEntity<Page<Order>> getGuestOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<Order> orders = orderService.getGuestOrders(pageable);
            
            log.info("Retrieved {} guest orders", orders.getTotalElements());
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            log.error("Error retrieving guest orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Inner class for order statistics
     */
    public static class OrderStatistics {
        private long totalOrders;
        private long paidOrders;
        private long pendingOrders;
        private long cancelledOrders;
        private long totalRevenue;
        
        public OrderStatistics(long totalOrders, long paidOrders, long pendingOrders, 
                             long cancelledOrders, long totalRevenue) {
            this.totalOrders = totalOrders;
            this.paidOrders = paidOrders;
            this.pendingOrders = pendingOrders;
            this.cancelledOrders = cancelledOrders;
            this.totalRevenue = totalRevenue;
        }
        
        // Getters
        public long getTotalOrders() { return totalOrders; }
        public long getPaidOrders() { return paidOrders; }
        public long getPendingOrders() { return pendingOrders; }
        public long getCancelledOrders() { return cancelledOrders; }
        public long getTotalRevenue() { return totalRevenue; }
    }
}

