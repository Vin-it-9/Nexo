package org.walletservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {
    private String currency;
    private String currencyFullName;
    private BigDecimal total;
    private BigDecimal available;
    private BigDecimal locked;
    private BigDecimal usdValue;
}