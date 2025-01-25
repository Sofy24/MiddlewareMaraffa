package server;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;

/*
 * This class is responsible for managing the WebSocket connections of the application.
 */
public class WebSocketVertx {
	 private final Map<UUID, ServerWebSocket> activeConnections;
    private final Set<String> activeUsers;
    private final Map<String, Long> pendingMessages; // Tracks pending messages with retry counts
    private final Vertx vertx; // Vertx instance for timers

    public WebSocketVertx(final Vertx vertx) {
        this.activeConnections = new ConcurrentHashMap<>();
        this.activeUsers = ConcurrentHashMap.newKeySet();
        this.pendingMessages = new ConcurrentHashMap<>();
        this.vertx = vertx;
    }

    public Set<String> getActiveUsers() {
        return this.activeUsers;
    }

    public void handleWebSocket(final ServerWebSocket webSocket) {
        webSocket.accept();
        System.out.println("New WebSocket connection with ID: " + webSocket.path().split("/")[1]);
        this.activeConnections.put(UUID.fromString(webSocket.path().split("/")[1]), webSocket);
        this.activeUsers.add(webSocket.path().split("/")[2]);

        System.out.println("New WebSocket connection with : " + webSocket.path());
        System.out.println("New WebSocket connection from user: " + webSocket.path().split("/")[2]);
        System.out.println("Active users: " + this.activeUsers);

        webSocket.handler(buffer -> {
            // Handle incoming message
            // Convert buffer to string
            String message = buffer.toString();
            System.out.println("Received message from " + webSocket.path().split("/")[1] + ": " + message);

            if (message.startsWith("\"") && message.endsWith("\"")) {
                message = message.substring(1, message.length() - 1).replace("\\\"", "\"");
            }

            // Parse JSON string
            final JsonObject jsonMessage = new JsonObject(message);

            System.out.println("Decoded " + webSocket.path().split("/")[1] + ": " + jsonMessage.toString());
            if ("ack".equals(jsonMessage.getString("type")) && jsonMessage.containsKey("messageId")) {
            final String messageId = jsonMessage.getString("messageId");

            if (this.pendingMessages.containsKey(messageId)) {
                this.pendingMessages.remove(messageId);
                System.out.println("Acknowledgment received for messageId: " + messageId);
            }
        } else {
            // Handle other message types if necessary
            System.out.println("Non-ack message received: " + jsonMessage);
        }
            // Check if this is an acknowledgment for a pending message
            // if (this.pendingMessages.containsKey(message)) {
            //     this.pendingMessages.remove(message);
            //     System.out.println("Acknowledgment received for messageId: " + message);
            // }
        });

        webSocket.closeHandler(v -> {
            this.activeUsers.remove(webSocket.path().split("/")[2]);
            System.out.println("WebSocket connection closed with ID: " + webSocket.path().split("/")[1]);
        });
    }

    public void sendMessageToClientWithRetry(final UUID clientID, final String message, final int timeoutMs, final int maxRetries) {
        final String messageId = UUID.randomUUID().toString(); // Unique ID for the message
        String fullMessage;
        // new JsonObject().
        // (message)
        //         .append(" | messageId: ").append(messageId)
        //         .toString();

                try {
                    final JsonObject jsonMessage = new JsonObject(message); // Converti la stringa JSON in un JsonObject
                    jsonMessage.put("messageId", messageId);          // Aggiungi il campo `messageId`
                    fullMessage = jsonMessage.toString();             // Convertilo nuovamente in stringa
                } catch (final Exception e) {
                    throw new IllegalArgumentException("Il messaggio fornito non è un JSON valido: " + message, e);
                }

        this.pendingMessages.put(messageId, 0L); // Initial retry count
        this.sendWithRetry(clientID, fullMessage, messageId, timeoutMs, maxRetries);
    }

    private void sendWithRetry(final UUID clientID, final String message, final String messageId, final int timeoutMs, final int maxRetries) {
        if (this.pendingMessages.getOrDefault(messageId, 0L) >= maxRetries) {
            this.pendingMessages.remove(messageId);
            System.err.println("Max retries reached for messageId: " + messageId);
            return;
        }

        System.out.println("Sending message to client: " + clientID + " with messageId: " + messageId);
        final ServerWebSocket webSocket = this.activeConnections.get(clientID);
        if (webSocket != null) {
            webSocket.writeTextMessage(message);

            this.vertx.setTimer(timeoutMs, timerId -> {
                if (this.pendingMessages.containsKey(messageId)) {
                    final long retryCount = this.pendingMessages.computeIfPresent(messageId, (key, val) -> val + 1);
                    System.out.println("Retrying messageId: " + messageId + ", attempt: " + retryCount);
                    this.sendWithRetry(clientID, message, messageId, timeoutMs, maxRetries);
                }
            });
        } else {
            System.out.println("No active connection with ID: " + clientID);
        }
    }

    public void broadcastToEveryoneWithRetry(final String message, final int timeoutMs, final int maxRetries) {
        for (final UUID clientID : this.activeConnections.keySet()) {
            this.sendMessageToClientWithRetry(clientID, message, timeoutMs, maxRetries);
        }
    }

}
