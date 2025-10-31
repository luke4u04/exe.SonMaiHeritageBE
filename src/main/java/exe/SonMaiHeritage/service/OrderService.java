package exe.SonMaiHeritage.service;

import exe.SonMaiHeritage.controller.OrderController;
import exe.SonMaiHeritage.entity.Order;
import exe.SonMaiHeritage.model.CheckoutRequest;
import exe.SonMaiHeritage.model.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    Order createOrder(CheckoutRequest checkoutRequest);
    Order getOrderByCode(String orderCode);
    void updateOrderStatus(String orderCode, Order.OrderStatus status);
    
    // New methods for order management
    Page<Order> getAllOrders(Pageable pageable);
    Order getOrderById(Integer orderId);
    List<Order> getOrdersByUserId(Integer userId);
    List<Order> getOrdersByStatus(Order.OrderStatus status);
    List<Order> getPaidOrders();
    List<Order> getGuestOrders();
    Page<Order> getGuestOrders(Pageable pageable);
    Order updateOrderStatus(Integer orderId, Order.OrderStatus status);
    OrderController.OrderStatistics getOrderStatistics();
    void processOrderPayment(String orderCode);
    OrderResponse convertToOrderResponse(Order order);
    List<OrderResponse> convertToOrderResponseList(List<Order> orders);
}
