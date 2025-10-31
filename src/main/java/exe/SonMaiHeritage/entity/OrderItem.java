package exe.SonMaiHeritage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="order_items")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    @Column(name="product_id")
    private Integer productId;
    
    @Column(name="product_name")
    private String productName;
    
    @Column(name="product_price")
    private Long productPrice;
    
    @Column(name="quantity")
    private Integer quantity;
    
    @Column(name="total_price")
    private Long totalPrice;
    
    @Column(name="product_image")
    private String productImage;
}
