package exe.SonMaiHeritage.controller;

import exe.SonMaiHeritage.entity.Product;
import exe.SonMaiHeritage.entity.Type;
import exe.SonMaiHeritage.model.AdminProductResponse;
import exe.SonMaiHeritage.repository.ProductRepository;
import exe.SonMaiHeritage.repository.TypeRepository;
import exe.SonMaiHeritage.service.LocalFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AdminProductController {

    private final ProductRepository productRepository;
    private final TypeRepository typeRepository;
    private final LocalFileService localFileService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> addProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Long price,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("typeId") Integer typeId,
            @RequestParam("image") MultipartFile image) {
        
        try {
            // Validate required fields
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Tên sản phẩm là bắt buộc"));
            }
            
            if (price == null || price <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Giá sản phẩm phải lớn hơn 0"));
            }
            
            if (quantity == null || quantity < 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Số lượng không được âm"));
            }
            
            if (typeId == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Loại sản phẩm là bắt buộc"));
            }

            // Validate image
            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ảnh sản phẩm là bắt buộc"));
            }

            if (!isImageFile(image)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "File phải là ảnh (JPG, PNG, GIF)"));
            }

            if (image.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Kích thước ảnh không được vượt quá 5MB"));
            }

            // Check if type exists
            Type type = typeRepository.findById(typeId)
                .orElseThrow(() -> new RuntimeException("Loại sản phẩm không tồn tại"));

            // Upload image
            String imageUrl = localFileService.uploadProductImage(image);
            log.info("Image uploaded successfully: {}", imageUrl);

            // Create product
            Product product = Product.builder()
                .name(name.trim())
                .description(description != null ? description.trim() : "")
                .price(price)
                .quantity(quantity)
                .pictureUrl(imageUrl)
                .type(type)
                .status(Product.ProductStatus.ACTIVE) // Default to ACTIVE
                .build();

            Product savedProduct = productRepository.save(product);
            log.info("Product created successfully: {}", savedProduct.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thêm sản phẩm thành công");
            response.put("product", Map.of(
                "id", savedProduct.getId(),
                "name", savedProduct.getName(),
                "price", savedProduct.getPrice(),
                "imageUrl", savedProduct.getPictureUrl()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error adding product: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Lỗi khi thêm sản phẩm: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<AdminProductResponse>> getAllProducts(
            @RequestParam(name = "status", required = false) String status) {
        try {
            // First check total products in database
            long totalProducts = productRepository.count();
            log.info("Total products in database: {}", totalProducts);
            
            List<Product> products;
            
            // Filter by status if provided
            if (status != null && !status.isEmpty()) {
                try {
                    Product.ProductStatus productStatus = Product.ProductStatus.valueOf(status.toUpperCase());
                    products = productRepository.findByStatusWithType(productStatus);
                    log.info("Found {} products with status {} and type", products.size(), status);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid status parameter: {}", status);
                    products = productRepository.findAllWithType();
                }
            } else {
                // Get all products with type (for admin view)
                products = productRepository.findAllWithType();
                log.info("Found {} products with type for admin", products.size());
            }
            
            // If no products with type, try without type join
            if (products.isEmpty() && totalProducts > 0) {
                log.info("No products with type found, trying without type join");
                List<Product> allProducts = productRepository.findAll();
                log.info("Found {} products without type join", allProducts.size());
                
                // Convert to DTO
                List<AdminProductResponse> responses = allProducts.stream()
                    .map(this::convertToAdminProductResponse)
                    .toList();
                
                return ResponseEntity.ok(responses);
            }
            
            // Convert to DTO to avoid circular reference
            List<AdminProductResponse> responses = products.stream()
                .map(this::convertToAdminProductResponse)
                .toList();
            
            log.info("Returning {} products to admin", responses.size());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error fetching products: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Admin products endpoint is working!");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Integer id) {
        try {
            Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("Error fetching product: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable Integer id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "price", required = false) Long price,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            @RequestParam(value = "typeId", required = false) Integer typeId,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        
        try {
            Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

            // Update fields if provided
            if (name != null && !name.trim().isEmpty()) {
                product.setName(name.trim());
            }
            
            if (description != null) {
                product.setDescription(description.trim());
            }
            
            if (price != null && price > 0) {
                product.setPrice(price);
            }
            
            if (quantity != null && quantity >= 0) {
                product.setQuantity(quantity);
            }
            
            if (typeId != null) {
                Type type = typeRepository.findById(typeId)
                    .orElseThrow(() -> new RuntimeException("Loại sản phẩm không tồn tại"));
                product.setType(type);
            }
            
            if (image != null && !image.isEmpty()) {
                if (!isImageFile(image)) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "File phải là ảnh (JPG, PNG, GIF)"));
                }
                
                if (image.getSize() > 5 * 1024 * 1024) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Kích thước ảnh không được vượt quá 5MB"));
                }
                
                String imageUrl = localFileService.uploadProductImage(image);
                product.setPictureUrl(imageUrl);
            }

            Product updatedProduct = productRepository.save(product);
            log.info("Product updated successfully: {}", updatedProduct.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật sản phẩm thành công");
            response.put("product", Map.of(
                "id", updatedProduct.getId(),
                "name", updatedProduct.getName(),
                "price", updatedProduct.getPrice(),
                "imageUrl", updatedProduct.getPictureUrl()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating product: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Lỗi khi cập nhật sản phẩm: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Integer id) {
        try {
            Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

            productRepository.delete(product);
            log.info("Product deleted successfully: {}", id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa sản phẩm thành công");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting product: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Lỗi khi xóa sản phẩm: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateProductStatus(
            @PathVariable Integer id,
            @RequestParam("status") String status) {
        
        try {
            Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

            // Validate status
            Product.ProductStatus newStatus;
            try {
                newStatus = Product.ProductStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Trạng thái không hợp lệ. Chỉ chấp nhận: ACTIVE, INACTIVE, DISCONTINUED"));
            }

            product.setStatus(newStatus);
            Product updatedProduct = productRepository.save(product);
            log.info("Product status updated: {} -> {}", id, newStatus);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật trạng thái sản phẩm thành công");
            response.put("product", Map.of(
                "id", updatedProduct.getId(),
                "name", updatedProduct.getName(),
                "status", updatedProduct.getStatus()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating product status: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Lỗi khi cập nhật trạng thái sản phẩm: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/quantity")
    public ResponseEntity<Map<String, Object>> updateProductQuantity(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

            // Validate quantity
            Object quantityObj = requestBody.get("quantity");
            if (quantityObj == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Số lượng là bắt buộc"));
            }

            Integer newQuantity;
            try {
                newQuantity = Integer.valueOf(quantityObj.toString());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Số lượng phải là số nguyên"));
            }

            if (newQuantity < 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Số lượng không được âm"));
            }

            product.setQuantity(newQuantity);
            Product updatedProduct = productRepository.save(product);
            log.info("Product quantity updated: {} -> {} (product: {})", id, newQuantity, product.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật số lượng sản phẩm thành công");
            response.put("product", Map.of(
                "id", updatedProduct.getId(),
                "name", updatedProduct.getName(),
                "quantity", updatedProduct.getQuantity()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating product quantity: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Lỗi khi cập nhật số lượng sản phẩm: " + e.getMessage()));
        }
    }

    private AdminProductResponse convertToAdminProductResponse(Product product) {
        return AdminProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .quantity(product.getQuantity())
            .pictureUrl(product.getPictureUrl())
            .status(product.getStatus() != null ? product.getStatus().toString() : "ACTIVE")
            .typeName(product.getType() != null ? product.getType().getName() : "Unknown")
            .typeId(product.getType() != null ? product.getType().getId() : null)
            .build();
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/jpg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/gif") ||
            contentType.equals("image/webp")
        );
    }
}
