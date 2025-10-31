package exe.SonMaiHeritage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOSResponse {
    private boolean success;
    private String message;
    private String paymentUrl;
    private String paymentCode;
    private String orderCode;
    private Long amount;
    private String status;
}
