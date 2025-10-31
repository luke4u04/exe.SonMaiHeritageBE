package exe.SonMaiHeritage.service;

import exe.SonMaiHeritage.entity.Order;
import exe.SonMaiHeritage.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    
    public void sendSimpleOrderConfirmationEmail(Order order) {
        try {
            log.info("Sending simple order confirmation email to: {}", order.getShipEmail());
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(order.getShipEmail());
            message.setSubject("Xác nhận đơn hàng #" + order.getOrderCode() + " - Son Mai Heritage");
            message.setFrom("noreply@sonmaiheritage.com");
            
            StringBuilder emailContent = new StringBuilder();
            emailContent.append("Xin chào ").append(order.getShipFullName()).append(",\n\n");
            emailContent.append("Cảm ơn bạn đã đặt hàng tại Son Mai Heritage!\n\n");
            emailContent.append("Thông tin đơn hàng:\n");
            emailContent.append("Mã đơn hàng: ").append(order.getOrderCode()).append("\n");
            emailContent.append("Ngày đặt: ").append(order.getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
            emailContent.append("Trạng thái: ").append(getStatusText(order.getStatus())).append("\n\n");
            
            emailContent.append("Sản phẩm đã đặt:\n");
            for (OrderItem item : order.getOrderItems()) {
                emailContent.append("- ").append(item.getProductName())
                    .append(" x").append(item.getQuantity())
                    .append(" = ").append(formatPrice(item.getTotalPrice())).append("\n");
            }
            
            emailContent.append("\nTổng tiền: ").append(formatPrice(order.getTotalAmount())).append("\n\n");
            
            emailContent.append("Thông tin giao hàng:\n");
            emailContent.append("Họ tên: ").append(order.getShipFullName()).append("\n");
            emailContent.append("Số điện thoại: ").append(order.getShipPhone()).append("\n");
            emailContent.append("Email: ").append(order.getShipEmail()).append("\n");
            emailContent.append("Địa chỉ: ").append(buildShippingAddress(order)).append("\n\n");
            
            if (order.getNote() != null && !order.getNote().trim().isEmpty()) {
                emailContent.append("Ghi chú: ").append(order.getNote()).append("\n\n");
            }
            
            emailContent.append("Chúng tôi sẽ liên hệ với bạn trong thời gian sớm nhất để xác nhận đơn hàng.\n\n");
            emailContent.append("Trân trọng,\n");
            emailContent.append("Son Mai Heritage Team");
            
            message.setText(emailContent.toString());
            mailSender.send(message);
            
            log.info("Simple order confirmation email sent successfully to: {}", order.getShipEmail());
            
        } catch (Exception e) {
            log.error("Failed to send simple order confirmation email to: {}, error: {}", 
                order.getShipEmail(), e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
    
    private String buildShippingAddress(Order order) {
        return order.getShipStreet() + ", " + 
               order.getShipWard() + ", " + 
               order.getShipDistrict() + ", " + 
               order.getShipProvince();
    }
    
    private String formatPrice(Long price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }
    
    private String getStatusText(Order.OrderStatus status) {
        switch (status) {
            case PENDING: return "Chờ xử lý";
            case CONFIRMED: return "Đã xác nhận";
            case SHIPPING: return "Đang giao hàng";
            case DELIVERED: return "Đã giao hàng";
            case CANCELLED: return "Đã hủy";
            default: return "Không xác định";
        }
    }
}
