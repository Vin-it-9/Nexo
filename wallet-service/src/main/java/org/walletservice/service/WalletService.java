package org.walletservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.walletservice.dto.*;
import org.walletservice.entity.*;
import org.walletservice.exception.InsufficientBalanceException;
import org.walletservice.exception.ResourceNotFoundException;
import org.walletservice.repository.BalanceRepository;
import org.walletservice.repository.TransactionRepository;
import org.walletservice.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private PriceFeedService priceFeedService;

    /**
     * Create a new wallet for a user
     */
    @Transactional
    public WalletResponse createWallet(String userId) {
        if (walletRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("Wallet already exists for user: " + userId);
        }

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .balances(new ArrayList<>())
                .build();

        walletRepository.save(wallet);

        for (CryptoCurrency currency : CryptoCurrency.values()) {
            Balance balance = Balance.builder()
                    .wallet(wallet)
                    .currency(currency)
                    .amount(BigDecimal.ZERO)
                    .availableAmount(BigDecimal.ZERO)
                    .build();

            wallet.getBalances().add(balance);
        }

        walletRepository.save(wallet);
        return mapToWalletResponse(wallet);
    }

    /**
     * Get a user's wallet details
     */
    @Transactional(readOnly = true)
    public WalletResponse getWallet(String userId) {
        Wallet wallet = findWalletByUserId(userId);
        return mapToWalletResponse(wallet);
    }

    /**
     * Process a deposit transaction
     */
    @Transactional
    public TransactionResponse deposit(String userId, TransactionRequest request) {
        Wallet wallet = findWalletByUserId(userId);

        Balance balance = findOrCreateBalance(wallet, request.getCurrency());

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .transactionId(generateTransactionId())
                .currency(request.getCurrency())
                .type(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .address(request.getAddress())
                .status(TransactionStatus.COMPLETED)
                .build();

        transactionRepository.save(transaction);

        balance.setAmount(balance.getAmount().add(request.getAmount()));
        balance.setAvailableAmount(balance.getAvailableAmount().add(request.getAmount()));
        balanceRepository.save(balance);

        return mapToTransactionResponse(transaction);
    }

    /**
     * Process a withdrawal transaction
     */
    @Transactional
    public TransactionResponse withdraw(String userId, TransactionRequest request) {
        Wallet wallet = findWalletByUserId(userId);

        Balance balance = findBalance(wallet, request.getCurrency());

        if (balance.getAvailableAmount().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient available balance. Available: " + balance.getAvailableAmount() +
                            ", Requested: " + request.getAmount());
        }

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .transactionId(generateTransactionId())
                .currency(request.getCurrency())
                .type(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .address(request.getAddress())
                .status(TransactionStatus.PENDING)
                .build();

        transactionRepository.save(transaction);

        balance.setAvailableAmount(balance.getAvailableAmount().subtract(request.getAmount()));

        balanceRepository.save(balance);
        completeWithdrawal(transaction.getTransactionId());

        return mapToTransactionResponse(transaction);
    }

    /**
     * Complete a pending withdrawal
     */
    @Transactional
    public TransactionResponse completeWithdrawal(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));

        if (transaction.getType() != TransactionType.WITHDRAWAL) {
            throw new IllegalArgumentException("Not a withdrawal transaction: " + transactionId);
        }

        if (transaction.getStatus() == TransactionStatus.COMPLETED) {
            return mapToTransactionResponse(transaction);
        }

        Balance balance = findBalance(transaction.getWallet(), transaction.getCurrency());

        balance.setAmount(balance.getAmount().subtract(transaction.getAmount()));
        balanceRepository.save(balance);

        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);

        return mapToTransactionResponse(transaction);
    }

    /**
     * Lock funds for collateral
     */
    @Transactional
    public TransactionResponse lockCollateral(String userId, TransactionRequest request) {
        Wallet wallet = findWalletByUserId(userId);

        Balance balance = findBalance(wallet, request.getCurrency());

        if (balance.getAvailableAmount().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient available balance. Available: " + balance.getAvailableAmount() +
                            ", Requested: " + request.getAmount());
        }

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .transactionId(generateTransactionId())
                .currency(request.getCurrency())
                .type(TransactionType.LOAN_COLLATERAL)
                .amount(request.getAmount())
                .status(TransactionStatus.COMPLETED)
                .build();

        transactionRepository.save(transaction);

        balance.setAvailableAmount(balance.getAvailableAmount().subtract(request.getAmount()));
        balanceRepository.save(balance);

        return mapToTransactionResponse(transaction);
    }

    /**
     * Release collateral back to available balance
     */
    @Transactional
    public TransactionResponse releaseCollateral(String userId, TransactionRequest request) {
        Wallet wallet = findWalletByUserId(userId);

        Balance balance = findBalance(wallet, request.getCurrency());

        BigDecimal lockedAmount = balance.getAmount().subtract(balance.getAvailableAmount());
        if (lockedAmount.compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient locked balance. Locked: " + lockedAmount +
                            ", Requested to release: " + request.getAmount());
        }

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .transactionId(generateTransactionId())
                .currency(request.getCurrency())
                .type(TransactionType.LOAN_RELEASE)
                .amount(request.getAmount())
                .status(TransactionStatus.COMPLETED)
                .build();

        transactionRepository.save(transaction);
        balance.setAvailableAmount(balance.getAvailableAmount().add(request.getAmount()));
        balanceRepository.save(balance);

        return mapToTransactionResponse(transaction);
    }

    /**
     * Process a liquidation transaction
     */
    @Transactional
    public TransactionResponse liquidateCollateral(String userId, TransactionRequest request) {
        Wallet wallet = findWalletByUserId(userId);

        Balance balance = findBalance(wallet, request.getCurrency());

        BigDecimal lockedAmount = balance.getAmount().subtract(balance.getAvailableAmount());
        if (lockedAmount.compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient locked balance. Locked: " + lockedAmount +
                            ", Requested to liquidate: " + request.getAmount());
        }

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .transactionId(generateTransactionId())
                .currency(request.getCurrency())
                .type(TransactionType.LIQUIDATION)
                .amount(request.getAmount())
                .status(TransactionStatus.COMPLETED)
                .build();

        transactionRepository.save(transaction);
        balance.setAmount(balance.getAmount().subtract(request.getAmount()));
        balanceRepository.save(balance);

        return mapToTransactionResponse(transaction);
    }

    /**
     * Get transaction history for a user
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionHistory(String userId, int page, int size) {
        Wallet wallet = findWalletByUserId(userId);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Transaction> transactions = transactionRepository.findByWallet(wallet, pageRequest);

        return transactions.map(this::mapToTransactionResponse);
    }

    /**
     * Get a specific transaction by ID
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(String userId, String transactionId) {
        Wallet wallet = findWalletByUserId(userId);

        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));

        if (!transaction.getWallet().getId().equals(wallet.getId())) {
            throw new IllegalArgumentException("Transaction does not belong to this user");
        }

        return mapToTransactionResponse(transaction);
    }

    /**
     * Helper method to find a wallet by user ID
     */
    private Wallet findWalletByUserId(String userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));
    }

    /**
     * Helper method to find a balance by wallet and currency
     */
    private Balance findBalance(Wallet wallet, CryptoCurrency currency) {
        return balanceRepository.findByWalletAndCurrency(wallet, currency)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Balance not found for wallet: " + wallet.getId() + " and currency: " + currency));
    }

    /**
     * Helper method to find or create a balance by wallet and currency
     */
    private Balance findOrCreateBalance(Wallet wallet, CryptoCurrency currency) {
        return balanceRepository.findByWalletAndCurrency(wallet, currency)
                .orElseGet(() -> {
                    Balance newBalance = Balance.builder()
                            .wallet(wallet)
                            .currency(currency)
                            .amount(BigDecimal.ZERO)
                            .availableAmount(BigDecimal.ZERO)
                            .build();
                    return balanceRepository.save(newBalance);
                });
    }

    /**
     * Generate a unique transaction ID
     */
    private String generateTransactionId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Map wallet entity to response DTO
     */
    private WalletResponse mapToWalletResponse(Wallet wallet) {
        List<BalanceResponse> balanceResponses = wallet.getBalances().stream()
                .map(this::mapToBalanceResponse)
                .collect(Collectors.toList());

        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balances(balanceResponses)
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    /**
     * Map balance entity to response DTO
     */
    private BalanceResponse mapToBalanceResponse(Balance balance) {
        BigDecimal locked = balance.getAmount().subtract(balance.getAvailableAmount());

        BigDecimal priceInUsd = priceFeedService.getCurrentPrice(balance.getCurrency().name());
        BigDecimal usdValue = balance.getAmount().multiply(priceInUsd);

        return BalanceResponse.builder()
                .currency(balance.getCurrency().name())
                .currencyFullName(balance.getCurrency().getFullName())
                .total(balance.getAmount())
                .available(balance.getAvailableAmount())
                .locked(locked)
                .usdValue(usdValue)
                .build();
    }

    /**
     * Map transaction entity to response DTO
     */
    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .currency(transaction.getCurrency().name())
                .currencyFullName(transaction.getCurrency().getFullName())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .address(transaction.getAddress())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}