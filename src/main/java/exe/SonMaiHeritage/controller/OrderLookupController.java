package exe.SonMaiHeritage.controller;

import exe.SonMaiHeritage.entity.Order;
import exe.SonMaiHeritage.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class OrderLookupController {

    private final OrderRepository orderRepository;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        log.info("🔧 Test endpoint called!");
        log.info("🔧 OrderLookupController initialized and working!");
        return ResponseEntity.ok("OrderLookupController is working!");
    }

    @GetMapping("/lookup/{orderCode}")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<?> lookupOrder(@PathVariable String orderCode) {
        try {
            log.info("🔧 OrderLookupController.lookupOrder called with code: {}", orderCode);
            
            // Tìm đơn hàng theo mã đơn hàng
            Optional<Order> orderOpt = orderRepository.findByOrderCode(orderCode);
            
            if (orderOpt.isEmpty()) {
                log.info("🔧 Order not found for code: {}", orderCode);
                return ResponseEntity.notFound().build();
            }
            
            Order order = orderOpt.get();
            log.info("🔧 Order found: {} with status: {}", orderCode, order.getStatus());
            
            // Tạo response DTO
            OrderLookupResponse response = OrderLookupResponse.builder()
                .orderCode(order.getOrderCode())
                .status(order.getStatus().toString())
                .createdAt(order.getCreatedDate().toString())
                .shipName(order.getShipFullName())
                .shipPhone(order.getShipPhone())
                .shipEmail(order.getShipEmail())
                .shipStreet(order.getShipStreet())
                .shipWard(order.getShipWard())
                .shipDistrict(order.getShipDistrict())
                .shipProvince(order.getShipProvince())
                .totalAmount(order.getTotalAmount().doubleValue())
                .note(order.getNote())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error looking up order: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body("Có lỗi xảy ra khi tra cứu đơn hàng: " + e.getMessage());
        }
    }

    // Response DTO
    @lombok.Data
    @lombok.Builder
    public static class OrderLookupResponse {
        private String orderCode;
        private String status;
        private String createdAt;
        private String shipName;
        private String shipPhone;
        private String shipEmail;
        private String shipStreet;
        private String shipWard;
        private String shipDistrict;
        private String shipProvince;
        private Double totalAmount;
        private String note;
    }
}
