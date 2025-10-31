package exe.SonMaiHeritage.repository;

import exe.SonMaiHeritage.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByPaymentCode(String paymentCode);
    
    List<Payment> findByStatus(Payment.PaymentStatus status);
    long countByStatus(Payment.PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId")
    Optional<Payment> findByOrderId(@Param("orderId") Integer orderId);
    
    @Query("SELECT SUM(p.amount) FROM Payment p")
    Long sumTotalAmount();
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status")
    Long sumAmountByStatus(@Param("status") Payment.PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.order.user.id = :userId ORDER BY p.createdDate DESC")
    List<Payment> findByOrderUserId(@Param("userId") Integer userId);
}
