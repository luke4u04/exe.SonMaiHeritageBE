package exe.SonMaiHeritage.service;

import exe.SonMaiHeritage.model.CheckoutRequest;
import exe.SonMaiHeritage.model.PayOSResponse;

import java.util.Map;

public interface PayOSService {
    PayOSResponse createPaymentLink(CheckoutRequest checkoutRequest, String paymentCode);
    PayOSResponse handlePaymentReturn(Map<String, String> params);
    boolean validateWebhook(Map<String, Object> webhookData);
}
