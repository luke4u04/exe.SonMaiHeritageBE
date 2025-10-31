package exe.SonMaiHeritage.controller;

import exe.SonMaiHeritage.entity.Payment;
import exe.SonMaiHeritage.model.PaymentResponse;
import exe.SonMaiHeritage.service.PaymentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing payments and payment history
 */
@RestController
@RequestMapping("/api/payments")
@Log4j2
public class PaymentController {
    
    private final PaymentService paymentService;
    
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    /**
     * Get all payments with pagination and sorting
     */
    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<PaymentResponse> payments = paymentService.getAllPaymentsAsResponse(pageable);
            
            log.info("Retrieved {} payments", payments.getTotalElements());
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            log.error("Error retrieving payments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get payment by ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Integer paymentId) {
        try {
            PaymentResponse payment = paymentService.getPaymentByIdAsResponse(paymentId);
            log.info("Retrieved payment with ID: {}", paymentId);
            return ResponseEntity.ok(payment);
            
        } catch (Exception e) {
            log.error("Error retrieving payment with ID {}: {}", paymentId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get payment by payment code
     */
    @GetMapping("/code/{paymentCode}")
    public ResponseEntity<PaymentResponse> getPaymentByCode(@PathVariable String paymentCode) {
        try {
            PaymentResponse payment = paymentService.getPaymentByCodeAsResponse(paymentCode);
            log.info("Retrieved payment with code: {}", paymentCode);
            return ResponseEntity.ok(payment);
            
        } catch (Exception e) {
            log.error("Error retrieving payment with code {}: {}", paymentCode, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    
    /**
     * Get payments by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(@PathVariable String status) {
        try {
            Payment.PaymentStatus paymentStatus = Payment.PaymentStatus.valueOf(status.toUpperCase());
            List<PaymentResponse> payments = paymentService.getPaymentsByStatusAsResponse(paymentStatus);
            
            log.info("Retrieved {} payments with status {}", payments.size(), status);
            return ResponseEntity.ok(payments);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid payment status: {}", status);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error retrieving payments by status {}: {}", status, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get successful payments only
     */
    @GetMapping("/successful")
    public ResponseEntity<List<Payment>> getSuccessfulPayments() {
        try {
            List<Payment> successfulPayments = paymentService.getSuccessfulPayments();
            log.info("Retrieved {} successful payments", successfulPayments.size());
            return ResponseEntity.ok(successfulPayments);
            
        } catch (Exception e) {
            log.error("Error retrieving successful payments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get payments by order ID
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Integer orderId) {
        try {
            PaymentResponse payment = paymentService.getPaymentByOrderIdAsResponse(orderId);
            log.info("Retrieved payment for order ID: {}", orderId);
            return ResponseEntity.ok(payment);
            
        } catch (Exception e) {
            log.error("Error retrieving payment for order ID {}: {}", orderId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get payments by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable Integer userId) {
        try {
            List<PaymentResponse> payments = paymentService.getPaymentsByUserIdAsResponse(userId);
            log.info("Retrieved {} payments for user ID: {}", payments.size(), userId);
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            log.error("Error retrieving payments for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get payment statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<PaymentStatistics> getPaymentStatistics() {
        try {
            PaymentStatistics stats = paymentService.getPaymentStatistics();
            log.info("Retrieved payment statistics");
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error retrieving payment statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Inner class for payment statistics
     */
    public static class PaymentStatistics {
        private long totalPayments;
        private long successfulPayments;
        private long failedPayments;
        private long pendingPayments;
        private long totalAmount;
        private long successfulAmount;
        
        public PaymentStatistics(long totalPayments, long successfulPayments, long failedPayments,
                               long pendingPayments, long totalAmount, long successfulAmount) {
            this.totalPayments = totalPayments;
            this.successfulPayments = successfulPayments;
            this.failedPayments = failedPayments;
            this.pendingPayments = pendingPayments;
            this.totalAmount = totalAmount;
            this.successfulAmount = successfulAmount;
        }
        
        // Getters
        public long getTotalPayments() { return totalPayments; }
        public long getSuccessfulPayments() { return successfulPayments; }
        public long getFailedPayments() { return failedPayments; }
        public long getPendingPayments() { return pendingPayments; }
        public long getTotalAmount() { return totalAmount; }
        public long getSuccessfulAmount() { return successfulAmount; }
    }
}

