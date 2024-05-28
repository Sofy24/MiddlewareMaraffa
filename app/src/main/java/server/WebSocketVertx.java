package server;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import game.service.User;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;

public class WebSocketVertx {
    private final Map<InGameUser, Optional<ServerWebSocket>> activeConnections;

    public WebSocketVertx() {
        this.activeConnections = new ConcurrentHashMap<>();
    }

    public void handleWebSocket(final ServerWebSocket webSocket) {
        if (this.activeConnections.keySet().stream()
                .map(InGameUser::user)
                .map(User::clientID)
                .toList().contains(UUID.fromString(webSocket.path().split("/")[1]))) { // FA un po' cagare ma non avevo
                                                                                       // grandi idee....
            webSocket.accept();
            this.getUsersToUpdate(webSocket);
            webSocket.handler(buffer -> {
                // Handle incoming message
                final String message = buffer.toString();
                System.out.println("Received message from " + webSocket.path().split("/")[1] + ": " + message);

                // Echo the message back to the client
                webSocket.writeTextMessage("Echo: " + message);
            });

            webSocket.closeHandler(v -> {
                // activeConnections.remove(connectionId);
                System.out.println("WebSocket connection closed with ID: " + webSocket.path().split("/")[1]);
            });

        } else {
            System.out.println("Rejected connection with ID: " + webSocket.path().split("/")[1]);
            webSocket.reject();
        }
        /*
         * if ("/websocket".equals(webSocket.path())) {
         * System.out.println(webSocket.path());
         * webSocket.accept();
         * 
         * final String connectionId = webSocket.textHandlerID();
         * // activeConnections.put(connectionId, webSocket);
         * System.out.println("New WebSocket connection with ID: " + connectionId);
         * 
         * webSocket.handler(buffer -> {
         * // Handle incoming message
         * final String message = buffer.toString();
         * System.out.println("Received message from " + connectionId + ": " + message);
         * 
         * // Echo the message back to the client
         * webSocket.writeTextMessage("Echo: " + message);
         * });
         * 
         * webSocket.closeHandler(v -> {
         * // activeConnections.remove(connectionId);
         * System.out.println("WebSocket connection closed with ID: " + connectionId);
         * });
         * } else {
         * webSocket.reject();
         * }
         */
    }

    private void getUsersToUpdate(final ServerWebSocket webSocket) {
        final List<InGameUser> usersToUpdate = this.activeConnections.keySet().stream()
                .filter(inGameUser -> inGameUser.user().clientID()
                        .equals(UUID.fromString(webSocket.path().split("/")[1]))
                        && this.activeConnections.get(inGameUser).isEmpty())
                .collect(Collectors.toList());
        for (final InGameUser inGameUser : usersToUpdate) {
            this.activeConnections.put(inGameUser, Optional.of(webSocket));
        }
    }

    public void addConnetedUser(final User user, final UUID gameID) {
        System.out.println("Adding user to active connections");
        System.out.println("User: " + user.toString());
        System.out.println(this.activeConnections.toString());
        this.activeConnections.put(new InGameUser(user, gameID), Optional.empty());
    }

    public List<ServerWebSocket> findWebSocketsByClientID(final UUID clientID) {
        return this.activeConnections.entrySet().stream()
                .filter(entry -> entry.getKey().user().clientID().equals(clientID) && entry.getValue().isPresent())
                .map(entry -> entry.getValue().get())
                .collect(Collectors.toList());
    }

    public void sendMessageToClientBuffer(final UUID clientID, final Buffer message) {
        System.out.println("Sending message to client: " + clientID.toString());
        final List<ServerWebSocket> webSocket = this.findWebSocketsByClientID(clientID);
        if (webSocket.size() > 0) {
            for (final ServerWebSocket serverWebSocket : webSocket) {
                serverWebSocket.write(message);
            }
        } else {
            System.out.println("No active connection with ID: " + clientID.toString());
        }
    }

    public void sendMessageToClient(final UUID clientID, final String message) {
        final List<ServerWebSocket> webSocket = this.findWebSocketsByClientID(clientID);
        if (webSocket.size() > 0) {
            for (final ServerWebSocket serverWebSocket : webSocket) {
                System.out.println("writing to: " + serverWebSocket.path());
                serverWebSocket.writeTextMessage(message);
            }
        } else {
            System.out.println("No active connection with ID: " + clientID.toString());
        }
    }
}
