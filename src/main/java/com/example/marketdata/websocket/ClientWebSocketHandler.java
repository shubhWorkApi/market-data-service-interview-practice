package com.example.marketdata.websocket;

import com.example.marketdata.service.OrderBookService;
import com.example.marketdata.service.SessionManager;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ClientWebSocketHandler extends TextWebSocketHandler {

    private final SessionManager sessionManager;
    private final OrderBookService orderBookService;

    public ClientWebSocketHandler(
            SessionManager sessionManager,
            OrderBookService orderBookService) {

        this.sessionManager = sessionManager;
        this.orderBookService = orderBookService;
    }

    @Override
    public void afterConnectionEstablished(
            WebSocketSession session) {

        String clientId = getClientId(session);

        /*
         * SessionManager enforces the single-session-per-client rule.
         */
        sessionManager.register(clientId, session);
    }

    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message) {

        String payload = message.getPayload().trim();

        /*
         * Expected message:
         * {"symbol":"BTC-USDT"}
         */
        if (payload.contains("\"symbol\"")) {

            String symbol = payload.replaceAll(
                    ".*\"symbol\"\\s*:\\s*\"([^\"]+)\".*",
                    "$1"
            );

            orderBookService.subscribe(
                    getClientId(session),
                    symbol,
                    session
            );
        }
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status) {

        String clientId = getClientId(session);

        /*
         * Remove only this exact WebSocket session.
         */
        sessionManager.remove(session);

        /*
         * Prevent an old session from deleting a newer session.
         */
        orderBookService.disconnect(
                clientId,
                session
        );
    }

    private String getClientId(
            WebSocketSession session) {

        String query =
                session.getUri() == null
                        ? null
                        : session.getUri().getQuery();

        if (query != null &&
                query.startsWith("clientId=")) {

            return query.substring(
                    "clientId=".length()
            );
        }

        return session.getId();
    }
}