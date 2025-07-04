package nl.bjornvanderlaan.adyenpayments.mcpserver;

import com.adyen.model.checkout.PaymentMethod;
import com.adyen.model.checkout.PaymentMethodsRequest;
import com.adyen.service.checkout.PaymentsApi;
import com.adyen.Client;
import com.adyen.enums.Environment;
import com.adyen.service.exception.ApiException;

import java.io.IOException;
import java.util.List;

public class AdyenPaymentService {
    private final PaymentsApi paymentsApi;

    public AdyenPaymentService(AppConfig config) {
        Client client = new Client(config.getApiKey(), Environment.valueOf(config.getApiEnvironment()));
        this.paymentsApi = new PaymentsApi(client);
    }

    public List<PaymentMethod> getPaymentMethods(String merchantAccount, String countryCode) throws IOException, ApiException {
        PaymentMethodsRequest request = new PaymentMethodsRequest();
        request.setMerchantAccount(merchantAccount);
        request.setCountryCode(countryCode);
        request.setChannel(PaymentMethodsRequest.ChannelEnum.WEB);

        return paymentsApi.paymentMethods(request).getPaymentMethods();
    }
}
