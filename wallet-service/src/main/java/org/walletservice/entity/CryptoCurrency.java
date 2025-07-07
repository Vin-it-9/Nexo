package org.walletservice.entity;

public enum CryptoCurrency {
    BTC("Bitcoin"),
    ETH("Ethereum"),
    USDT("Tether"),
    USDC("USD Coin"),
    BNB("Binance Coin"),
    SOL("Solana"),
    ADA("Cardano"),
    XRP("Ripple"),
    DOT("Polkadot"),
    DOGE("Dogecoin");

    private final String fullName;

    CryptoCurrency(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }
}