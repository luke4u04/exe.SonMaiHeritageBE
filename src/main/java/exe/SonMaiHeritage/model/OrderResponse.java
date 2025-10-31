package exe.SonMaiHeritage.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import exe.SonMaiHeritage.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private Integer id;
    private String orderCode;
    private Integer userId;
    private String userFullName;
    private Long totalAmount;
    private Order.OrderStatus status;
    private String paymentMethod;
    private String paymentStatus;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedDate;
    
    // Shipping information
    private String shipFullName;
    private String shipPhone;
    private String shipStreet;
    private String shipWard;
    private String shipDistrict;
    private String shipProvince;
    
    // Order items
    private List<OrderItemResponse> orderItems;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemResponse {
        private Integer id;
        private Integer productId;
        private String productName;
        private Long productPrice;
        private Integer quantity;
        private Long totalPrice;
        private String productImage;
    }
}






