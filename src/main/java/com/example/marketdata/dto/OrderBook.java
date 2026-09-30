package com.example.marketdata.dto;

import java.util.List;

public record OrderBook(
        String symbol,
        List<Level> bids,
        List<Level> asks
) {
    public record Level(double price, double quantity) {}
}
