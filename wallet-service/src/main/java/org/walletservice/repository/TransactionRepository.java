package org.walletservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.walletservice.entity.Transaction;
import org.walletservice.entity.Wallet;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    Optional<Transaction> findByTransactionId(String transactionId);
    Page<Transaction> findByWallet(Wallet wallet, Pageable pageable);
}