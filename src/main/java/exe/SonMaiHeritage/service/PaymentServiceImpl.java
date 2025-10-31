package exe.SonMaiHeritage.service;

import exe.SonMaiHeritage.controller.PaymentController;
import exe.SonMaiHeritage.entity.Payment;
import exe.SonMaiHeritage.entity.Order;
import exe.SonMaiHeritage.entity.User;
import exe.SonMaiHeritage.model.PaymentResponse;
import exe.SonMaiHeritage.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepository;
    
    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }
    
    @Override
    public Page<Payment> getAllPayments(Pageable pageable) {
        log.info("Retrieving all payments with pagination");
        return paymentRepository.findAll(pageable);
    }
    
    @Override
    public Payment getPaymentById(Integer paymentId) {
        log.info("Retrieving payment by ID: {}", paymentId);
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));
    }
    
    @Override
    public Payment getPaymentByCode(String paymentCode) {
        log.info("Retrieving payment by code: {}", paymentCode);
        return paymentRepository.findByPaymentCode(paymentCode)
                .orElseThrow(() -> new RuntimeException("Payment not found with code: " + paymentCode));
    }
    
    
    @Override
    public List<Payment> getPaymentsByStatus(Payment.PaymentStatus status) {
        log.info("Retrieving payments by status: {}", status);
        return paymentRepository.findByStatus(status);
    }
    
    @Override
    public List<Payment> getSuccessfulPayments() {
        log.info("Retrieving successful payments");
        return paymentRepository.findByStatus(Payment.PaymentStatus.SUCCESS);
    }
    
    @Override
    public Payment getPaymentByOrderId(Integer orderId) {
        log.info("Retrieving payment by order ID: {}", orderId);
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order ID: " + orderId));
    }
    
    @Override
    public PaymentController.PaymentStatistics getPaymentStatistics() {
        log.info("Calculating payment statistics");
        
        long totalPayments = paymentRepository.count();
        long successfulPayments = paymentRepository.countByStatus(Payment.PaymentStatus.SUCCESS);
        long failedPayments = paymentRepository.countByStatus(Payment.PaymentStatus.FAILED);
        long pendingPayments = paymentRepository.countByStatus(Payment.PaymentStatus.PENDING);
        
        Long totalAmount = paymentRepository.sumTotalAmount();
        Long successfulAmount = paymentRepository.sumAmountByStatus(Payment.PaymentStatus.SUCCESS);
        
        return new PaymentController.PaymentStatistics(
                totalPayments,
                successfulPayments,
                failedPayments,
                pendingPayments,
                totalAmount != null ? totalAmount : 0L,
                successfulAmount != null ? successfulAmount : 0L
        );
    }
    
    @Override
    public Payment createPayment(Payment payment) {
        log.info("Creating new payment with code: {}", payment.getPaymentCode());
        return paymentRepository.save(payment);
    }
    
    @Override
    public Payment updatePaymentStatus(Integer paymentId, Payment.PaymentStatus status) {
        log.info("Updating payment {} status to {}", paymentId, status);
        
        Payment payment = getPaymentById(paymentId);
        payment.setStatus(status);
        
        return paymentRepository.save(payment);
    }
    
    @Override
    public List<Payment> getPaymentsByUserId(Integer userId) {
        log.info("Retrieving payments for user ID: {}", userId);
        return paymentRepository.findByOrderUserId(userId);
    }
    
    // New DTO response methods
    @Override
    public Page<PaymentResponse> getAllPaymentsAsResponse(Pageable pageable) {
        log.info("Retrieving all payments as response with pagination");
        Page<Payment> payments = paymentRepository.findAll(pageable);
        List<PaymentResponse> paymentResponses = convertToPaymentResponseList(payments.getContent());
        return new PageImpl<>(paymentResponses, pageable, payments.getTotalElements());
    }
    
    @Override
    public PaymentResponse getPaymentByIdAsResponse(Integer paymentId) {
        log.info("Retrieving payment by ID as response: {}", paymentId);
        Payment payment = getPaymentById(paymentId);
        return convertToPaymentResponse(payment);
    }
    
    @Override
    public PaymentResponse getPaymentByCodeAsResponse(String paymentCode) {
        log.info("Retrieving payment by code as response: {}", paymentCode);
        Payment payment = getPaymentByCode(paymentCode);
        return convertToPaymentResponse(payment);
    }
    
    
    @Override
    public List<PaymentResponse> getPaymentsByStatusAsResponse(Payment.PaymentStatus status) {
        log.info("Retrieving payments by status as response: {}", status);
        List<Payment> payments = getPaymentsByStatus(status);
        return convertToPaymentResponseList(payments);
    }
    
    @Override
    public PaymentResponse getPaymentByOrderIdAsResponse(Integer orderId) {
        log.info("Retrieving payment by order ID as response: {}", orderId);
        Payment payment = getPaymentByOrderId(orderId);
        return convertToPaymentResponse(payment);
    }
    
    @Override
    public List<PaymentResponse> getPaymentsByUserIdAsResponse(Integer userId) {
        log.info("Retrieving payments for user ID as response: {}", userId);
        List<Payment> payments = getPaymentsByUserId(userId);
        return convertToPaymentResponseList(payments);
    }
    
    @Override
    public PaymentResponse convertToPaymentResponse(Payment payment) {
        if (payment == null) {
            return null;
        }
        
        PaymentResponse.OrderInfo orderInfo = null;
        if (payment.getOrder() != null) {
            Order order = payment.getOrder();
            User user = order.getUser();
            
            orderInfo = PaymentResponse.OrderInfo.builder()
                    .id(order.getId())
                    .orderCode(order.getOrderCode())
                    .totalAmount(order.getTotalAmount())
                    .status(order.getStatus() != null ? order.getStatus().toString() : null)
                    .userFullName(user != null ? (user.getFirstName() + " " + user.getLastName()).trim() : null)
                    .userEmail(user != null ? user.getEmail() : null)
                    .build();
        }
        
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentCode(payment.getPaymentCode())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .createdDate(payment.getCreatedDate())
                .updatedDate(payment.getUpdatedDate())
                .paymentUrl(payment.getPaymentUrl())
                .order(orderInfo)
                .build();
    }
    
    @Override
    public List<PaymentResponse> convertToPaymentResponseList(List<Payment> payments) {
        if (payments == null) {
            return null;
        }
        
        return payments.stream()
                .map(this::convertToPaymentResponse)
                .collect(Collectors.toList());
    }
}

