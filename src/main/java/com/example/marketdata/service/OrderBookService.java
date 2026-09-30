package com.example.marketdata.service;

import com.example.marketdata.client.OkxWebSocketClient;
import com.example.marketdata.dto.OrderBook;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.WebSocket;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderBookService {

    private final OkxWebSocketClient okxClient;
    private final ObjectMapper objectMapper;

    // One OKX WebSocket subscription is maintained per trading symbol.
    private final Map<String, WebSocket> okxSubscriptions = new ConcurrentHashMap<>();

    // Currently active browser session for each client.
    private final Map<String, WebSocketSession> browserSubscribers = new ConcurrentHashMap<>();

    // Symbol currently selected by each client.
    private final Map<String, String> clientSymbols = new ConcurrentHashMap<>();

    public OrderBookService(
            OkxWebSocketClient okxClient,
            ObjectMapper objectMapper) {

        this.okxClient = okxClient;
        this.objectMapper = objectMapper;
    }

    public void subscribe(
            String clientId,
            String symbol,
            WebSocketSession browserSession) {

        // Store the currently active browser session.
        browserSubscribers.put(clientId, browserSession);

        // Remember which symbol this client wants.
        clientSymbols.put(clientId, symbol);

        // Reuse one OKX subscription for the same symbol.
        okxSubscriptions.computeIfAbsent(
                symbol,
                key -> okxClient.subscribe(
                        key,
                        orderBook -> broadcast(key, orderBook)
                )
        );
    }

    private void broadcast(String symbol, OrderBook orderBook) {

        browserSubscribers.forEach((clientId, browserSession) -> {

            // Only send this order book to clients watching this symbol.
            if (!symbol.equals(clientSymbols.get(clientId))) {
                return;
            }

            // Remove closed browser connections.
            if (!browserSession.isOpen()) {
                disconnect(clientId, browserSession);
                return;
            }

            try {
                String payload =
                        objectMapper.writeValueAsString(orderBook);

                browserSession.sendMessage(
                        new TextMessage(payload)
                );

            } catch (Exception ignored) {
                // Remove broken browser connections.
                disconnect(clientId, browserSession);
            }
        });
    }

    public void disconnect(
            String clientId,
            WebSocketSession session) {

        /*
         * IMPORTANT:
         * Remove the session only if this exact session is still
         * registered for the client.
         *
         * This prevents an old WebSocket's close callback from
         * accidentally removing a newer WebSocket session.
         */
        boolean removed =
                browserSubscribers.remove(clientId, session);

        if (removed) {
            clientSymbols.remove(clientId);
        }

        cleanupUnusedSubscriptions();
    }

    private void cleanupUnusedSubscriptions() {

        okxSubscriptions.forEach((symbol, okxWebSocket) -> {

            boolean stillUsed =
                    clientSymbols.containsValue(symbol);

            if (!stillUsed &&
                    okxSubscriptions.remove(symbol, okxWebSocket)) {

                try {
                    okxWebSocket.close(
                            1000,
                            "No browser subscribers"
                    );
                } catch (Exception ignored) {
                    // Socket may already be closed.
                }
            }
        });
    }
}