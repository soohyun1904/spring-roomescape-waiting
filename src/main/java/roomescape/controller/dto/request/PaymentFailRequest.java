package roomescape.controller.dto.request;

// 사용자가 결제창에서 결제를 취소하면(PAY_PROCESS_CANCELED) orderId가 없을 수 있어 모든 필드가 선택 값이다.
public class PaymentFailRequest {
    private final String code;
    private final String message;
    private final String orderId;

    public PaymentFailRequest(String code, String message, String orderId) {
        this.code = code;
        this.message = message;
        this.orderId = orderId;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getOrderId() {
        return orderId;
    }
}
