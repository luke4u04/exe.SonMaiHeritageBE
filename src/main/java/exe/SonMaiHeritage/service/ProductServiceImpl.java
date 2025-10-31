package exe.SonMaiHeritage.service;

import exe.SonMaiHeritage.entity.Product;
import exe.SonMaiHeritage.exceptions.ProductNotFoundException;
import exe.SonMaiHeritage.model.ProductResponse;
import exe.SonMaiHeritage.repository.ProductRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductResponse getProductById(Integer productId) {
        log.info("Fetching Product by Id: {}", productId);
        Product product =productRepository.findById(productId)
                .orElseThrow(()->new ProductNotFoundException("Product with given id doesn't exist"));
        //now convert the product to product response
        ProductResponse productResponse = convertToProductResponse(product);
        log.info("Fetched Product by Id: {}", productId);
        return productResponse;
    }

    @Override
    public Page<ProductResponse> getProducts(Pageable pageable) {
        log.info("Fetching ACTIVE products");
        //Retrieve only ACTIVE products from DB
        List<Product> activeProducts = productRepository.findAllActiveWithType();
        // Convert to Page manually since we're filtering
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), activeProducts.size());
        List<Product> pageContent = activeProducts.subList(start, end);
        
        Page<Product> productPage = new org.springframework.data.domain.PageImpl<>(
            pageContent, pageable, activeProducts.size());
        
        //Map
        Page<ProductResponse> productResponses = productPage
                .map(this::convertToProductResponse);
        log.info("Fetched {} ACTIVE products", productResponses.getTotalElements());
        return productResponses;
    }

    @Override
    public List<ProductResponse> searchProductsByName(String keyword) {
        log.info("Searching ACTIVE product(s) by name: {}", keyword);
        //Call the custom query Method - search in name only
        List<Product> products = productRepository.searchByNameOrDescriptionOrType(keyword);
        // Filter to only include ACTIVE products
        List<Product> activeProducts = products.stream()
                .filter(product -> product.getStatus() == Product.ProductStatus.ACTIVE)
                .collect(Collectors.toList());
        //Map
        List<ProductResponse> productResponses = activeProducts.stream()
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());
        log.info("Fetched {} ACTIVE products", productResponses.size());
        return productResponses;
    }

    @Override
    public List<ProductResponse> searchProductsByType(Integer typeId) {
        log.info("Searching ACTIVE product(s) by typeId: {}", typeId);
        //Call the custom query Method
        List<Product> products = productRepository.searchByType(typeId);
        // Filter to only include ACTIVE products
        List<Product> activeProducts = products.stream()
                .filter(product -> product.getStatus() == Product.ProductStatus.ACTIVE)
                .collect(Collectors.toList());
        //Map
        List<ProductResponse> productResponses = activeProducts.stream()
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());
        log.info("Fetched {} ACTIVE products", productResponses.size());
        return productResponses;
    }

    @Override
    public void updateProductQuantity(Integer productId, Integer quantityToSubtract) {
        log.info("Updating quantity for product ID: {}, subtracting: {}", productId, quantityToSubtract);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product with given id doesn't exist"));
        
        int newQuantity = product.getQuantity() - quantityToSubtract;
        if (newQuantity < 0) {
            throw new RuntimeException("Insufficient product quantity. Available: " + product.getQuantity() + ", Requested: " + quantityToSubtract);
        }
        
        product.setQuantity(newQuantity);
        productRepository.save(product);
        log.info("Updated product quantity. New quantity: {}", newQuantity);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all ACTIVE products without pagination");
        // Only return ACTIVE products for customer-facing API
        List<Product> products = productRepository.findAllActiveWithType();
        return products.stream()
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse convertToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .pictureUrl(product.getPictureUrl())
                .quantity(product.getQuantity())
                .productType(product.getType() != null ? product.getType().getName() : "Chưa phân loại")
                .build();
    }
}
