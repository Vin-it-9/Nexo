package org.walletservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.walletservice.entity.Balance;
import org.walletservice.entity.CryptoCurrency;
import org.walletservice.entity.Wallet;

import java.util.Optional;

public interface BalanceRepository extends JpaRepository<Balance, Integer> {
    Optional<Balance> findByWalletAndCurrency(Wallet wallet, CryptoCurrency currency);
}