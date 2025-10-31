package exe.SonMaiHeritage.controller;

import exe.SonMaiHeritage.config.DataSeeder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AdminController {
    
    private final DataSeeder dataSeeder;
    
    public AdminController(DataSeeder dataSeeder) {
        this.dataSeeder = dataSeeder;
    }
    
    @PostMapping("/recreate-data")
    public ResponseEntity<String> recreateData() {
        try {
            dataSeeder.forceRecreateData();
            return ResponseEntity.ok("Data recreated successfully with UTF-8 encoding");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error recreating data: " + e.getMessage());
        }
    }

    @PostMapping("/seed-new-data")
    public ResponseEntity<String> seedNewData() {
        try {
            dataSeeder.seedNewProductData();
            return ResponseEntity.ok("New product data seeded successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error seeding new data: " + e.getMessage());
        }
    }

    @PostMapping("/seed-sample-orders")
    public ResponseEntity<String> seedSampleOrders() {
        try {
            dataSeeder.seedSampleOrders();
            return ResponseEntity.ok("Sample orders seeded successfully with UTF-8 encoding");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error seeding sample orders: " + e.getMessage());
        }
    }
    
    @PostMapping("/check-data")
    public ResponseEntity<Map<String, Object>> checkData() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("products", dataSeeder.getProductCount());
            response.put("types", dataSeeder.getTypeCount());
            response.put("users", dataSeeder.getUserCount());
            response.put("orders", dataSeeder.getOrderCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error checking data: " + e.getMessage()));
        }
    }
    
    @PostMapping("/seed-products")
    public ResponseEntity<String> seedProducts() {
        try {
            dataSeeder.seedProductsSimple();
            return ResponseEntity.ok("Products seeded successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error seeding products: " + e.getMessage());
        }
    }
    
    @PostMapping("/add-lacquer-paintings")
    public ResponseEntity<String> addLacquerPaintings() {
        try {
            dataSeeder.addLacquerPaintings();
            return ResponseEntity.ok("Lacquer paintings added successfully to database");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error adding lacquer paintings: " + e.getMessage());
        }
    }
    
    @GetMapping("/add-lacquer-paintings")
    public ResponseEntity<String> addLacquerPaintingsGet() {
        try {
            dataSeeder.addLacquerPaintings();
            return ResponseEntity.ok("Lacquer paintings added successfully to database");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error adding lacquer paintings: " + e.getMessage());
        }
    }
    
}
