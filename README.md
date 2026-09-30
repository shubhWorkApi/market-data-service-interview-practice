# Market Data Service - Interview Practice

## Overview

A Spring Boot service that acts as a proxy between a browser client and the public OKX market-data APIs.

The browser never calls OKX directly. The backend owns the external OKX REST and WebSocket connections and exposes REST and WebSocket endpoints for browser clients.

## Features

- Hardcoded demo login for the interview assignment
- Top 20 spot markets by 24h quote volume
- REST-based market overview
- Live OKX `books5` order-book streaming
- Top 15 bid and ask levels displayed in the browser
- Backend WebSocket proxy between OKX and browser clients
- Multiple independent browser clients supported concurrently
- Single-session enforcement per client ID
- Cleanup of disconnected browser sessions
- Cleanup of unused OKX symbol subscriptions

## Tech Stack

- Java 17
- Spring Boot
- Spring Web
- Spring WebSocket
- Maven
- OkHttp
- HTML/CSS/JavaScript

## Architecture

```text
Browser
   |
   +---------------- REST ----------------> Spring Boot
   |                                           |
   |                                           +----> OKX REST API
   |
   +------------- WebSocket ----------------> Spring Boot
                                               |
                                               +----> OKX WebSocket
                                                        |
                                                        +---- Order Book
```

The backend acts as the proxy and aggregator between browser clients and OKX. Browser clients never connect directly to OKX.

## How to Run

Requirements:

- JDK 17+
- Maven 3.9+
- Internet access for OKX public APIs

Run:

```bash
mvn spring-boot:run
```

Or open the project in IntelliJ IDEA and run:

`MarketDataApplication`

Then open:

```text
http://localhost:8080
```

Demo credentials:

- Username: admin
- Password: admin

## APIs

### Login

POST `/api/auth/login`

```json
{
  "username": "admin",
  "password": "admin"
}
```

### Market Overview

GET `/api/markets`

Returns the top 20 spot pairs sorted by 24h quote volume.

### Browser WebSocket

Connect to:

```text
ws://localhost:8080/ws/market?clientId=<unique-client-id>
```

Then send:

```json
{
  "symbol": "BTC-USDT"
}
```

The backend subscribes to the OKX `books5` channel and forwards live order-book updates to the browser.

The browser receives the top 15 bid and ask levels for the selected trading pair.

## Single-session enforcement

`SessionManager` maintains a concurrent map of `clientId -> WebSocketSession`.

When a client connects again using the same client ID, the previous open browser WebSocket is closed before the new session becomes active. This prevents multiple active browser sessions for the same logical client.

Multiple independent clients can maintain their own WebSocket connections and receive live order-book updates concurrently.

## WebSocket lifecycle and cleanup

The backend maintains the relationship between browser clients, selected symbols, and OKX WebSocket subscriptions.

When a browser subscribes to a symbol, the backend creates or reuses the corresponding OKX WebSocket subscription and forwards received order-book updates to the browser clients subscribed to that symbol.

When a browser disconnects, its WebSocket session is removed. Unused OKX symbol subscriptions are also cleaned up so that the backend does not keep unnecessary external connections open.

## Assumptions

- The assignment allows a hardcoded user store, so no database is used for login.
- OKX public APIs are used; no API key is required.
- The browser sends a unique client ID.
- Order book updates use the OKX public `books5` channel.
- The sample is intentionally small enough to understand and modify during an interview.
