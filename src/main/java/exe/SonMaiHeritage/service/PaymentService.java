package exe.SonMaiHeritage.service;

import exe.SonMaiHeritage.controller.PaymentController;
import exe.SonMaiHeritage.entity.Payment;
import exe.SonMaiHeritage.model.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentService {
    Page<Payment> getAllPayments(Pageable pageable);
    Page<PaymentResponse> getAllPaymentsAsResponse(Pageable pageable);
    Payment getPaymentById(Integer paymentId);
    PaymentResponse getPaymentByIdAsResponse(Integer paymentId);
    Payment getPaymentByCode(String paymentCode);
    PaymentResponse getPaymentByCodeAsResponse(String paymentCode);
    List<Payment> getPaymentsByStatus(Payment.PaymentStatus status);
    List<PaymentResponse> getPaymentsByStatusAsResponse(Payment.PaymentStatus status);
    List<Payment> getSuccessfulPayments();
    Payment getPaymentByOrderId(Integer orderId);
    PaymentResponse getPaymentByOrderIdAsResponse(Integer orderId);
    PaymentController.PaymentStatistics getPaymentStatistics();
    Payment createPayment(Payment payment);
    Payment updatePaymentStatus(Integer paymentId, Payment.PaymentStatus status);
    List<Payment> getPaymentsByUserId(Integer userId);
    List<PaymentResponse> getPaymentsByUserIdAsResponse(Integer userId);
    
    // Conversion methods
    PaymentResponse convertToPaymentResponse(Payment payment);
    List<PaymentResponse> convertToPaymentResponseList(List<Payment> payments);
}

