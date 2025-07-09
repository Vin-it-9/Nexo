package org.dto;

import lombok.Data;
import java.util.List;

@Data
public class CoinMarketCapResponse {
    private Status status;
    private List<CryptoCurrency> data;

    @Data
    public static class Status {
        private String timestamp;
        private int error_code;
        private String error_message;
        private int elapsed;
        private int credit_count;
    }
}