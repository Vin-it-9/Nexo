package org.walletservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {
    private Integer id;
    private String userId;
    private List<BalanceResponse> balances;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}