package com.example.marketdata.dto;

public record MarketTicker(
        String symbol,
        double lastPrice,
        double change24h,
        double volume24h
) {}
