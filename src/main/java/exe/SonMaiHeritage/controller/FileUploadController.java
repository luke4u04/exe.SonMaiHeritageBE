package exe.SonMaiHeritage.controller;

import exe.SonMaiHeritage.service.LocalFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final LocalFileService localFileService;

    @PostMapping("/product-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> uploadProductImage(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "File is empty"));
            }

            if (!isImageFile(file)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "File must be an image (jpg, jpeg, png, gif)"));
            }

            // Check file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "File size must be less than 5MB"));
            }

            String imageUrl = localFileService.uploadProductImage(file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("imageUrl", imageUrl);
            response.put("message", "Image uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error uploading product image: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error uploading image: " + e.getMessage()));
        }
    }

    @PostMapping("/user-avatar")
    public ResponseEntity<Map<String, Object>> uploadUserAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "File is empty"));
            }

            if (!isImageFile(file)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "File must be an image (jpg, jpeg, png, gif)"));
            }

            // Check file size (max 2MB)
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "File size must be less than 2MB"));
            }

            String imageUrl = localFileService.uploadUserAvatar(file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("imageUrl", imageUrl);
            response.put("message", "Avatar uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error uploading user avatar: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error uploading avatar: " + e.getMessage()));
        }
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
