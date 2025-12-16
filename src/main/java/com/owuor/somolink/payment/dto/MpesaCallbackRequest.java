package com.owuor.somolink.payment.dto;

import lombok.Data;

@Data
public class MpesaCallbackRequest {
    private Body body;

    @Data
    public static class Body {
        private StkCallback stkCallback;
    }

    @Data
    public static class StkCallback {
        private String MerchantRequestID;
        private String CheckoutRequestID;
        private int ResultCode;
        private String ResultDesc;
        private CallbackMetadata CallbackMetadata;
    }

    @Data
    public static class CallbackMetadata {
        private Item[] Item;

        @Data
        public static class Item {
            private String Name;
            private Object Value;
        }
    }
}
