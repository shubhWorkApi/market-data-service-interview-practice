package com.example.marketdata.controller;

import com.example.marketdata.dto.MarketTicker;
import com.example.marketdata.service.MarketDataService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/markets")
public class MarketController {

    private final MarketDataService marketDataService;

    public MarketController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @GetMapping
    public List<MarketTicker> getMarkets() throws IOException {
        return marketDataService.getTop20SpotPairs();
    }
}
