package exe.SonMaiHeritage.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryService {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    private Cloudinary getCloudinary() {
        if (cloudinary == null) {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
            ));
        }
        return cloudinary;
    }

    public String uploadFile(MultipartFile file, String folder) {
        try {
            Map<String, Object> params = ObjectUtils.asMap(
                "folder", folder,
                "public_id", java.util.UUID.randomUUID().toString(),
                "overwrite", true,
                "resource_type", "auto"
            );

            Map<?, ?> uploadResult = getCloudinary().uploader().upload(file.getBytes(), params);
            String imageUrl = (String) uploadResult.get("secure_url");
            
            log.info("File uploaded successfully to Cloudinary: {}", imageUrl);
            return imageUrl;

        } catch (IOException e) {
            log.error("Error uploading file to Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
    }

    public String uploadProductImage(MultipartFile file) {
        return uploadFile(file, "sonmai-heritage/products");
    }

    public String uploadUserAvatar(MultipartFile file) {
        return uploadFile(file, "sonmai-heritage/avatars");
    }
}
