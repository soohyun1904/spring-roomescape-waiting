package roomescape.controller.dto.response;

public class PaymentConfigResponse {
    private final String clientKey;

    public PaymentConfigResponse(String clientKey) {
        this.clientKey = clientKey;
    }

    public String getClientKey() {
        return clientKey;
    }
}
