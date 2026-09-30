package com.example.marketdata.service;

import com.example.marketdata.dto.MarketTicker;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MarketDataService {

    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;
    private final String restUrl;

    public MarketDataService(
            ObjectMapper objectMapper,
            @Value("${okx.rest-url}") String restUrl) {
        this.objectMapper = objectMapper;
        this.restUrl = restUrl;
        this.httpClient = new OkHttpClient();
    }

    public List<MarketTicker> getTop20SpotPairs() throws IOException {
        Request request = new Request.Builder()
                .url(restUrl + "/api/v5/market/tickers?instType=SPOT")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("OKX REST request failed: " + response.code());
            }

            JsonNode root = objectMapper.readTree(response.body().string());
            List<MarketTicker> result = new ArrayList<>();

            for (JsonNode item : root.path("data")) {
                String symbol = item.path("instId").asText();
                double last = item.path("last").asDouble();
                double open24h = item.path("open24h").asDouble();
                double volume24h = item.path("volCcy24h").asDouble();

                double change = open24h == 0
                        ? 0
                        : ((last - open24h) / open24h) * 100.0;

                result.add(new MarketTicker(symbol, last, change, volume24h));
            }

            return result.stream()
                    .sorted(Comparator.comparingDouble(MarketTicker::volume24h).reversed())
                    .limit(20)
                    .toList();
        }
    }
}
