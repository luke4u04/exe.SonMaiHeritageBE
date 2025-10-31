package exe.SonMaiHeritage.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckoutRequest {
    // Allow null for guest checkout
    private Integer userId;
    
    @NotEmpty(message = "Items list cannot be empty")
    private List<CartItemRequest> items;
    
    @NotNull(message = "Total amount is required")
    private Long totalAmount;
    
    private String note;

    // Shipping address info for this checkout
    @NotEmpty(message = "Full name is required")
    private String shipFullName;
    @NotEmpty(message = "Phone is required")
    private String shipPhone;
    @NotEmpty(message = "Email is required")
    private String shipEmail;
    @NotEmpty(message = "Street is required")
    private String shipStreet;
    @NotEmpty(message = "Ward is required")
    private String shipWard;
    @NotEmpty(message = "District is required")
    private String shipDistrict;
    @NotEmpty(message = "Province is required")
    private String shipProvince;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CartItemRequest {
        private Integer productId;
        private String productName;
        private Long productPrice;
        private Integer quantity;
        private String productImage;
    }
}
