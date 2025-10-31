package exe.SonMaiHeritage.service;

import exe.SonMaiHeritage.model.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductResponse getProductById(Integer productId);
    Page<ProductResponse> getProducts(Pageable pageable);
    List<ProductResponse> searchProductsByName(String keyword);
    List<ProductResponse> searchProductsByType(Integer typeId);
    void updateProductQuantity(Integer productId, Integer quantityToSubtract);
    List<ProductResponse> getAllProducts();
}
