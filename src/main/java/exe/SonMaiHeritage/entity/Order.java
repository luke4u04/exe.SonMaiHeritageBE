package exe.SonMaiHeritage.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;
    
    @Column(name="order_code", unique = true)
    private String orderCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name="total_amount")
    private Long totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private OrderStatus status;
    
    @Column(name="payment_method")
    private String paymentMethod;
    
    @Column(name="payment_status")
    private String paymentStatus;
    
    @Column(name="created_date")
    private LocalDateTime createdDate;
    
    @Column(name="updated_date")
    private LocalDateTime updatedDate;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    // Shipping snapshot fields (denormalized for historical accuracy)
    @Column(name="ship_full_name")
    private String shipFullName;

    @Column(name="ship_phone")
    private String shipPhone;

    @Column(name="ship_email")
    private String shipEmail;

    @Column(name="ship_street")
    private String shipStreet;

    @Column(name="ship_ward")
    private String shipWard;

    @Column(name="ship_district")
    private String shipDistrict;

    @Column(name="ship_province")
    private String shipProvince;
    
    @Column(name="note")
    private String note;
    
    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED
    }
}
