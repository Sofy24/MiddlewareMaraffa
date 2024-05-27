package server;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import game.service.User;
import io.vertx.core.http.ServerWebSocket;

public class WebSocketVertx {
    private final Map<InGameUser, Optional<ServerWebSocket>> activeConnections;

    public WebSocketVertx() {
        this.activeConnections = new ConcurrentHashMap<>();
    }

    public void handleWebSocket(final ServerWebSocket webSocket) {
        System.out.println(webSocket.path().split("/")[0]);
        if (this.activeConnections.keySet().stream()
                .map((Function<? super InGameUser, ? extends UUID>) InGameUser::gameID)
                .toList().contains(UUID.fromString(webSocket.path().split("/")[1]))) { //FA un po' cagare ma non avevo grandi idee....
            webSocket.accept();
            webSocket.handler(buffer -> {
                // Handle incoming message
                final String message = buffer.toString();
                System.out.println("Received message from " + webSocket.path().split("/")[1]  + ": " + message);

                // Echo the message back to the client
                webSocket.writeTextMessage("Echo: " + message);
            });

            webSocket.closeHandler(v -> {
                // activeConnections.remove(connectionId);
                System.out.println("WebSocket connection closed with ID: " + webSocket.path().split("/")[1]);
            });

        } else {
            webSocket.reject();
        }
        /*
        if ("/websocket".equals(webSocket.path())) {
            System.out.println(webSocket.path());
            webSocket.accept();

            final String connectionId = webSocket.textHandlerID();
            // activeConnections.put(connectionId, webSocket);
            System.out.println("New WebSocket connection with ID: " + connectionId);

            webSocket.handler(buffer -> {
                // Handle incoming message
                final String message = buffer.toString();
                System.out.println("Received message from " + connectionId + ": " + message);

                // Echo the message back to the client
                webSocket.writeTextMessage("Echo: " + message);
            });

            webSocket.closeHandler(v -> {
                // activeConnections.remove(connectionId);
                System.out.println("WebSocket connection closed with ID: " + connectionId);
            });
        } else {
            webSocket.reject();
        }*/
    }

    public void addConnetedUser(final User user, final UUID gameID) {
        System.out.println("Adding user to active connections");
        System.out.println("User: " + user.toString());
        System.out.println(this.activeConnections.toString());
        this.activeConnections.put(new InGameUser(user, gameID), Optional.empty());
    }

    public void sendMessageToClient(final String connectionId, final String message) {
        final ServerWebSocket webSocket = this.activeConnections.get(connectionId).get();
        if (webSocket != null) {
            webSocket.writeTextMessage(message);
        } else {
            System.out.println("No active connection with ID: " + connectionId);
        }
    }
}
