package exe.SonMaiHeritage.service;

import exe.SonMaiHeritage.config.PayOSConfig;
import exe.SonMaiHeritage.model.CheckoutRequest;
import exe.SonMaiHeritage.model.PayOSResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

import java.util.Map;

@Service
@Log4j2
public class PayOSServiceImpl implements PayOSService {

    private final PayOSConfig payOSConfig;
    private final PayOS payOS;

    public PayOSServiceImpl(PayOSConfig payOSConfig) {
        this.payOSConfig = payOSConfig;
        this.payOS = new PayOS(
            payOSConfig.getClientId(),
            payOSConfig.getApiKey(),
            payOSConfig.getChecksumKey()
        );
    }

    @Override
    public PayOSResponse createPaymentLink(CheckoutRequest checkoutRequest, String paymentCode) {
        try {
            log.info("Creating PayOS payment link for order: {}", paymentCode);
            
            // Extract order code
            String orderCode = paymentCode.split("_")[1];
            Long numericOrderCode = Long.parseLong(orderCode.substring(3));
            
            // Create ItemData for PayOS SDK
            ItemData itemData = ItemData.builder()
                    .name("Product")
                    .quantity(1)
                    .price(checkoutRequest.getTotalAmount().intValue())
                    .build();
            
            // Create PaymentData for PayOS SDK
            PaymentData paymentData = PaymentData.builder()
                    .orderCode(numericOrderCode)
                    .amount(checkoutRequest.getTotalAmount().intValue())
                    .description("Order " + numericOrderCode)
                    .returnUrl(payOSConfig.getReturnUrl())
                    .cancelUrl(payOSConfig.getCancelUrl())
                    .item(itemData)
                    .build();
            
            log.info("PayOS SDK request: {}", paymentData);
            
            // Use PayOS SDK to create payment link
            CheckoutResponseData result = payOS.createPaymentLink(paymentData);
            
            log.info("PayOS SDK response: {}", result);
            
            if (result != null && result.getCheckoutUrl() != null && !result.getCheckoutUrl().isEmpty()) {
                return PayOSResponse.builder()
                        .success(true)
                        .paymentUrl(result.getCheckoutUrl())
                        .paymentCode(paymentCode)
                        .orderCode(paymentCode)
                        .amount(checkoutRequest.getTotalAmount())
                        .message("Payment link created successfully")
                        .build();
            } else {
                log.error("PayOS SDK response missing checkoutUrl: {}", result);
                return PayOSResponse.builder()
                        .success(false)
                        .message("PayOS SDK response missing checkoutUrl")
                        .build();
            }

        } catch (Exception e) {
            log.error("Error creating PayOS payment link: {}", e.getMessage(), e);
            return PayOSResponse.builder()
                    .success(false)
                    .message("Error creating payment link: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public PayOSResponse handlePaymentReturn(Map<String, String> params) {
        try {
            log.info("Handling PayOS payment return with params: {}", params);
            
            // Log all parameters for debugging
            for (Map.Entry<String, String> entry : params.entrySet()) {
                log.info("PayOS param: {} = {}", entry.getKey(), entry.getValue());
            }
            
            String code = params.get("code");
            String desc = params.get("desc");
            String data = params.get("data");
            String orderCode = params.get("orderCode");
            String status = params.get("status");
            
            log.info("PayOS return - code: {}, desc: {}, data: {}, orderCode: {}, status: {}", 
                code, desc, data, orderCode, status);
            
            // Check multiple possible success indicators
            boolean isSuccess = "00".equals(code) || 
                              "PAID".equals(status) || 
                              "SUCCESS".equals(status) ||
                              (data != null && data.contains("success"));
            
            if (isSuccess) {
                log.info("Payment successful - code: {}, status: {}", code, status);
                return PayOSResponse.builder()
                        .success(true)
                        .message("Payment successful")
                        .status("SUCCESS")
                        .build();
            } else {
                log.warn("Payment failed - code: {}, desc: {}, status: {}", code, desc, status);
                return PayOSResponse.builder()
                        .success(false)
                        .message("Payment failed: " + (desc != null ? desc : "Unknown error"))
                        .status("FAILED")
                        .build();
            }
            
        } catch (Exception e) {
            log.error("Error handling PayOS payment return: {}", e.getMessage(), e);
            return PayOSResponse.builder()
                    .success(false)
                    .message("Error processing payment return: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean validateWebhook(Map<String, Object> webhookData) {
        try {
            // In production, implement proper signature validation
            // This is a simplified version
            return webhookData.containsKey("data") && webhookData.containsKey("type");
        } catch (Exception e) {
            log.error("Error validating PayOS webhook: {}", e.getMessage());
            return false;
        }
    }
    
    private String generateChecksum(Map<String, Object> data) {
        try {
            // Create a sorted string from the data for checksum according to PayOS format
            StringBuilder sb = new StringBuilder();
            sb.append("orderCode=").append(data.get("orderCode"));
            sb.append("&amount=").append(data.get("amount"));
            sb.append("&description=").append(data.get("description"));
            sb.append("&returnUrl=").append(data.get("returnUrl"));
            sb.append("&cancelUrl=").append(data.get("cancelUrl"));
            
            // Add items to checksum in PayOS format
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> items = (java.util.List<Map<String, Object>>) data.get("items");
            if (items != null) {
                for (Map<String, Object> item : items) {
                    String itemName = (String) item.get("name");
                    if (itemName != null) {
                        sb.append("&itemName=").append(itemName);
                    }
                    sb.append("&itemQuantity=").append(item.get("quantity"));
                    sb.append("&itemPrice=").append(item.get("price"));
                }
            }
            
            String dataString = sb.toString();
            log.info("Checksum data string: {}", dataString);
            
            // Generate HMAC SHA256 checksum
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                payOSConfig.getChecksumKey().getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(dataString.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            StringBuilder checksum = new StringBuilder();
            for (byte b : rawHmac) {
                checksum.append(String.format("%02x", b));
            }
            
            log.info("Generated checksum: {}", checksum.toString());
            return checksum.toString();
            
        } catch (Exception e) {
            log.error("Error generating checksum: {}", e.getMessage(), e);
            return "";
        }
    }
    
}
