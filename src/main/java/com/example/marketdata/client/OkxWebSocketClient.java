package com.example.marketdata.client;

import com.example.marketdata.dto.OrderBook;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component
public class OkxWebSocketClient {

    private final ObjectMapper objectMapper;
    private final String websocketUrl;
    private final OkHttpClient client = new OkHttpClient();

    public OkxWebSocketClient(
            ObjectMapper objectMapper,
            @Value("${okx.websocket-url}") String websocketUrl) {
        this.objectMapper = objectMapper;
        this.websocketUrl = websocketUrl;
    }

    public WebSocket subscribe(String symbol, Consumer<OrderBook> onUpdate) {
        Request request = new Request.Builder().url(websocketUrl).build();

        return client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                String subscribeMessage = """
                        {
                          "op": "subscribe",
                          "args": [
                            {
                              "channel": "books5",
                              "instId": "%s"
                            }
                          ]
                        }
                        """.formatted(symbol);

                webSocket.send(subscribeMessage);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JsonNode root = objectMapper.readTree(text);
                    JsonNode data = root.path("data");

                    if (!data.isArray() || data.isEmpty()) {
                        return;
                    }

                    JsonNode book = data.get(0);
                    List<OrderBook.Level> bids = parseLevels(book.path("bids"));
                    List<OrderBook.Level> asks = parseLevels(book.path("asks"));

                    onUpdate.accept(new OrderBook(symbol, bids, asks));
                } catch (Exception ignored) {
                    // Malformed/unexpected OKX messages are ignored for this demo.
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                // A production implementation should add bounded reconnect/backoff.
            }
        });
    }

    private List<OrderBook.Level> parseLevels(JsonNode levels) {
        List<OrderBook.Level> result = new ArrayList<>();

        for (JsonNode level : levels) {
            if (level.isArray() && level.size() >= 2) {
                result.add(new OrderBook.Level(
                        level.get(0).asDouble(),
                        level.get(1).asDouble()
                ));
            }
        }

        return result.stream().limit(15).toList();
    }
}
