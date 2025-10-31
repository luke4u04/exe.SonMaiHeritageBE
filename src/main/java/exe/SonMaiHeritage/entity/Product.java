package exe.SonMaiHeritage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="Product")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="Id")
    private Integer id;
    @Column(name="Name", columnDefinition = "VARCHAR(255)")
    private String name;
    @Column(name="Description", columnDefinition = "TEXT")
    private String description;
    @Column(name="Price")
    private Long price;
    @Column(name="PictureUrl")
    private String pictureUrl;
    
    @Column(name="Quantity")
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "ProductTypeId", referencedColumnName = "Id")
    private Type type;
    
    @Enumerated(EnumType.STRING)
    @Column(name="Status", columnDefinition = "VARCHAR(20) DEFAULT 'ACTIVE'")
    private ProductStatus status;
    
    public enum ProductStatus {
        ACTIVE,    // Còn kinh doanh
        INACTIVE,  // Ngừng kinh doanh
        DISCONTINUED // Ngừng sản xuất
    }
}
