package exe.SonMaiHeritage.service;

import exe.SonMaiHeritage.entity.Order;
import exe.SonMaiHeritage.entity.Payment;
import exe.SonMaiHeritage.model.CheckoutRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class MockPaymentService {

    @Value("${payment.mock.enabled:false}")
    private boolean mockEnabled;

    private final OrderService orderService;
    private final PaymentService paymentService;

    public MockPaymentService(OrderService orderService, PaymentService paymentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    public boolean processMockPayment(CheckoutRequest checkoutRequest, String paymentCode, Order order) {
        log.info("=== MOCK PAYMENT SERVICE ===");
        log.info("Processing mock payment for order: {}", order.getOrderCode());
        log.info("Total amount: {}", checkoutRequest.getTotalAmount());
        
        if (!mockEnabled) {
            log.warn("Mock payment is disabled. Please enable it in application.yaml");
            return false;
        }

        try {
            // Simulate payment processing delay
            Thread.sleep(1000); // 1 second delay to simulate processing
            
            // Process the order payment directly
            log.info("Processing order payment for: {}", order.getOrderCode());
            orderService.processOrderPayment(order.getOrderCode());
            
            // Update payment status to SUCCESS
            Payment payment = paymentService.getPaymentByCode(paymentCode);
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setUpdatedDate(java.time.LocalDateTime.now());
            paymentService.createPayment(payment);
            
            log.info("Mock payment processed successfully for order: {}", order.getOrderCode());
            return true;
                    
        } catch (Exception e) {
            log.error("Error processing mock payment: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean isMockEnabled() {
        return mockEnabled;
    }
}
