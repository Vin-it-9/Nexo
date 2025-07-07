package org.walletservice.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceUpdateEvent {
    private String symbol;
    private BigDecimal price;
    private BigDecimal change24h;
    private LocalDateTime timestamp;
}