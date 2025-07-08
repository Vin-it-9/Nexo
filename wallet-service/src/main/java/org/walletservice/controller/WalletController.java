package org.walletservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.walletservice.annotation.RateLimit;
import org.walletservice.dto.TransactionRequest;
import org.walletservice.dto.TransactionResponse;
import org.walletservice.dto.WalletResponse;
import org.walletservice.service.WalletService;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallet Management", description = "APIs for managing crypto wallets and transactions")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PostMapping
    @RateLimit
    @Operation(summary = "Create a new wallet", description = "Creates a new wallet for the authenticated user")
    public ResponseEntity<WalletResponse> createWallet(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(walletService.createWallet(userId));
    }

    @GetMapping("/my-wallet")
    @RateLimit
    @Operation(summary = "Get wallet details", description = "Retrieves the authenticated user's wallet details including all balances")
    public ResponseEntity<WalletResponse> getMyWallet(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(walletService.getWallet(userId));
    }

    @PostMapping("/deposit")
    @RateLimit
    @Operation(summary = "Make a deposit", description = "Simulates a deposit of cryptocurrency into the user's wallet")
    public ResponseEntity<TransactionResponse> deposit(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(walletService.deposit(userId, request));
    }

    @PostMapping("/withdraw")
    @RateLimit
    @Operation(summary = "Make a withdrawal", description = "Simulates a withdrawal of cryptocurrency from the user's wallet")
    public ResponseEntity<TransactionResponse> withdraw(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(walletService.withdraw(userId, request));
    }

    @GetMapping("/transactions")
    @RateLimit
    @Operation(summary = "Get transaction history", description = "Retrieves a paginated list of user's transactions")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(walletService.getTransactionHistory(userId, page, size));
    }

    @GetMapping("/transactions/{transactionId}")
    @RateLimit
    @Operation(summary = "Get transaction details", description = "Retrieves details of a specific transaction")
    public ResponseEntity<TransactionResponse> getTransaction(
            @AuthenticationPrincipal String userId,
            @PathVariable String transactionId) {
        return ResponseEntity.ok(walletService.getTransaction(userId, transactionId));
    }
}