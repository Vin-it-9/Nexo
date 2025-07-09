package org.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class CryptoCurrency {
    private int id;
    private String name;
    private String symbol;
    private String slug;
    private int cmc_rank;
    private int num_market_pairs;
    private BigDecimal circulating_supply;
    private BigDecimal total_supply;
    private BigDecimal max_supply;
    private String last_updated;
    private String date_added;
    private Map<String, Quote> quote;

    @Data
    public static class Quote {
        private BigDecimal price;
        private BigDecimal volume_24h;
        private BigDecimal volume_change_24h;
        private BigDecimal percent_change_1h;
        private BigDecimal percent_change_24h;
        private BigDecimal percent_change_7d;
        private BigDecimal percent_change_30d;
        private BigDecimal market_cap;
        private BigDecimal market_cap_dominance;
        private BigDecimal fully_diluted_market_cap;
        private String last_updated;
    }
}