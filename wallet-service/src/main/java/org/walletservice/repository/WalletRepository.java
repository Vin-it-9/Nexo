package org.walletservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.walletservice.entity.Wallet;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Integer> {
    Optional<Wallet> findByUserId(String userId);
    boolean existsByUserId(String userId);
}