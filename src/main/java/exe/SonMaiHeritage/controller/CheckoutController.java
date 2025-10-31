package exe.SonMaiHeritage.controller;

import exe.SonMaiHeritage.entity.Order;
import exe.SonMaiHeritage.entity.Payment;
import exe.SonMaiHeritage.model.CheckoutRequest;
import exe.SonMaiHeritage.model.PayOSResponse;
import exe.SonMaiHeritage.service.OrderService;
import exe.SonMaiHeritage.service.PaymentService;
import exe.SonMaiHeritage.service.MockPaymentService;
import exe.SonMaiHeritage.service.PayOSService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.net.URLEncoder;

@RestController
@RequestMapping("/api/checkout")
@Log4j2
public class CheckoutController {
    
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final MockPaymentService mockPaymentService;
    private final PayOSService payOSService;
    
    public CheckoutController(OrderService orderService, PaymentService paymentService, MockPaymentService mockPaymentService, PayOSService payOSService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.mockPaymentService = mockPaymentService;
        this.payOSService = payOSService;
    }
    
    @PostMapping("/simple")
    public ResponseEntity<Map<String, Object>> simpleCheckout(@Valid @RequestBody CheckoutRequest checkoutRequest) {
        try {
            log.info("=== SIMPLE CHECKOUT START ===");
            log.info("Processing simple checkout for user: {}", checkoutRequest.getUserId());
            log.info("CheckoutRequest details: totalAmount={}, itemsCount={}", 
                checkoutRequest.getTotalAmount(), checkoutRequest.getItems().size());
            log.info("Shipping info: fullName={}, phone={}, province={}", 
                checkoutRequest.getShipFullName(), checkoutRequest.getShipPhone(), checkoutRequest.getShipProvince());
            
            // Create order
            log.info("Creating order...");
            Order order = orderService.createOrder(checkoutRequest);
            log.info("Order created successfully: orderCode={}, orderId={}", order.getOrderCode(), order.getId());
            
            // Generate payment code
            String paymentCode = "PAY" + System.currentTimeMillis() + "_" + order.getOrderCode();
            log.info("Generated payment code: {}", paymentCode);
            
            // Create payment record with PENDING status
            log.info("Creating payment record...");
            Payment payment = Payment.builder()
                    .order(order)
                    .paymentCode(paymentCode)
                    .amount(checkoutRequest.getTotalAmount())
                    .paymentMethod("DIRECT")
                    .status(Payment.PaymentStatus.PENDING)
                    .createdDate(java.time.LocalDateTime.now())
                    .updatedDate(java.time.LocalDateTime.now())
                    .build();
            
            paymentService.createPayment(payment);
            log.info("Payment record created successfully with code: {}", paymentCode);
            
            // Note: Order remains in PENDING status, will be processed by admin later
            log.info("Order created with PENDING status, waiting for admin confirmation");
            
            log.info("=== SIMPLE CHECKOUT SUCCESS ===");
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "paymentCode", paymentCode,
                    "orderCode", order.getOrderCode(),
                    "message", "Order created and confirmed successfully"
            ));
            
        } catch (Exception e) {
            log.error("=== SIMPLE CHECKOUT ERROR ===");
            log.error("Error processing simple checkout: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Checkout failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/payos")
    public ResponseEntity<PayOSResponse> checkoutWithPayOS(@Valid @RequestBody CheckoutRequest checkoutRequest) {
        try {
            log.info("=== PAYOS CHECKOUT START ===");
            log.info("Processing PayOS checkout for user: {}", checkoutRequest.getUserId());
            log.info("CheckoutRequest details: totalAmount={}, itemsCount={}", 
                checkoutRequest.getTotalAmount(), checkoutRequest.getItems().size());
            
            // Create order
            log.info("Creating order...");
            Order order = orderService.createOrder(checkoutRequest);
            log.info("Order created successfully: orderCode={}, orderId={}", order.getOrderCode(), order.getId());
            
            // Generate payment code
            String paymentCode = "PAY" + System.currentTimeMillis() + "_" + order.getOrderCode();
            log.info("Generated payment code: {}", paymentCode);
            
            // Create payment record
            log.info("Creating payment record...");
            Payment payment = Payment.builder()
                    .order(order)
                    .paymentCode(paymentCode)
                    .amount(checkoutRequest.getTotalAmount())
                    .paymentMethod("PAYOS")
                    .status(Payment.PaymentStatus.PENDING)
                    .createdDate(java.time.LocalDateTime.now())
                    .updatedDate(java.time.LocalDateTime.now())
                    .build();
            
            paymentService.createPayment(payment);
            log.info("Payment record created successfully with code: {}", paymentCode);
            
            // Create PayOS payment link
            log.info("Creating PayOS payment link...");
            PayOSResponse payOSResponse = payOSService.createPaymentLink(checkoutRequest, paymentCode);
            
            if (payOSResponse.isSuccess()) {
                log.info("PayOS payment link created successfully: {}", payOSResponse.getPaymentUrl());
                log.info("=== PAYOS CHECKOUT SUCCESS ===");
                return ResponseEntity.ok(payOSResponse);
            } else {
                log.error("Failed to create PayOS payment link: {}", payOSResponse.getMessage());
                return ResponseEntity.badRequest().body(payOSResponse);
            }
            
        } catch (Exception e) {
            log.error("=== PAYOS CHECKOUT ERROR ===");
            log.error("Error processing PayOS checkout: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PayOSResponse.builder()
                            .success(false)
                            .message("Internal server error")
                            .build());
        }
    }
    
    @GetMapping("/payos/return")
    public ResponseEntity<Void> payOSReturn(@RequestParam Map<String, String> params) {
        try {
            log.info("=== PAYOS RETURN START ===");
            log.info("Processing PayOS return with params: {}", params);
            
            PayOSResponse payOSResponse = payOSService.handlePaymentReturn(params);
            
            if (payOSResponse.isSuccess()) {
                // Process successful payment
                String orderCode = params.get("orderCode");
                String fullOrderCode = null;
                if (orderCode != null) {
                    // PayOS sends numeric orderCode, but our system uses "ORD" prefix
                    fullOrderCode = "ORD" + orderCode;
                    log.info("Processing payment for order: {} (full code: {})", orderCode, fullOrderCode);
                    orderService.processOrderPayment(fullOrderCode);
                    log.info("Payment processed successfully for order: {}", fullOrderCode);
                }
                
                // Redirect to frontend with success parameters
                String redirectUrl = "http://localhost:4200/payment-result?success=true&orderCode=" + (fullOrderCode != null ? fullOrderCode : orderCode);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", redirectUrl)
                        .build();
            } else {
                log.warn("Payment failed: {}", payOSResponse.getMessage());
                // Redirect to frontend with failure parameters
                String redirectUrl = "http://localhost:4200/payment-result?success=false&message=" + 
                    java.net.URLEncoder.encode(payOSResponse.getMessage(), "UTF-8");
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", redirectUrl)
                        .build();
            }
            
        } catch (Exception e) {
            log.error("Error processing PayOS return: {}", e.getMessage());
            // Redirect to frontend with error
            try {
                String redirectUrl = "http://localhost:4200/payment-result?success=false&message=" + 
                    java.net.URLEncoder.encode("Lỗi xử lý thanh toán", "UTF-8");
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", redirectUrl)
                        .build();
            } catch (Exception ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }
    
    @PostMapping("/payos/webhook")
    public ResponseEntity<Map<String, String>> payOSWebhook(@RequestBody Map<String, Object> webhookData) {
        try {
            log.info("Processing PayOS webhook: {}", webhookData);
            
            // Validate webhook
            if (!payOSService.validateWebhook(webhookData)) {
                log.warn("Invalid PayOS webhook signature");
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Invalid signature"));
            }
            
            // Process webhook data
            Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
            String orderCode = (String) data.get("orderCode");
            String status = (String) data.get("status");
            
            if ("PAID".equals(status)) {
                // Process successful payment
                orderService.processOrderPayment(orderCode);
                log.info("Webhook processed successfully for order: {}", orderCode);
                return ResponseEntity.ok(Map.of("message", "Webhook processed successfully"));
            } else {
                log.warn("Payment not completed for order: {}", orderCode);
                return ResponseEntity.ok(Map.of("message", "Payment not completed"));
            }
            
        } catch (Exception e) {
            log.error("Error processing PayOS webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Webhook processing failed"));
        }
    }
    
    
    @GetMapping("/order/{orderCode}")
    public ResponseEntity<Order> getOrderDetails(@PathVariable String orderCode) {
        try {
            log.info("Fetching order details for code: {}", orderCode);
            Order order = orderService.getOrderByCode(orderCode);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error fetching order details: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
}
