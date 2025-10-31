package exe.SonMaiHeritage.repository;

import exe.SonMaiHeritage.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Integer> {
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByName(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByNameOrDescription(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByNameOrDescriptionOrType(@Param("keyword") String keyword);

    @Query("SELECT p FROM Product p WHERE p.type.id = :typeId")
    List<Product> searchByType(@Param("typeId") Integer typeId);
    
    @Query("SELECT p FROM Product p JOIN FETCH p.type")
    List<Product> findAllWithType();
    
    @Query("SELECT p FROM Product p JOIN FETCH p.type WHERE p.status = 'ACTIVE'")
    List<Product> findAllActiveWithType();
    
    @Query("SELECT p FROM Product p JOIN FETCH p.type WHERE p.status = :status")
    List<Product> findByStatusWithType(@Param("status") Product.ProductStatus status);
}
