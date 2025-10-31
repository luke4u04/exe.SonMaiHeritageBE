package exe.SonMaiHeritage.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import exe.SonMaiHeritage.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"id", "paymentCode", "amount", "paymentMethod", "status", "createdDate", "updatedDate", "paymentUrl", "order"})
public class PaymentResponse {
    private Integer id;
    private String paymentCode;
    private Long amount;
    private String paymentMethod;
    private Payment.PaymentStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedDate;
    
    private String paymentUrl;
    
    // Simplified order information to avoid circular references
    private OrderInfo order;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderInfo {
        private Integer id;
        private String orderCode;
        private Long totalAmount;
        private String status;
        private String userFullName;
        private String userEmail;
    }
}










