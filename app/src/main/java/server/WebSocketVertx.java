package server;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.vertx.core.http.ServerWebSocket;

/*
 * This class is responsible for managing the WebSocket connections of the application.
 */
public class WebSocketVertx {
	private final Map<UUID, ServerWebSocket> activeConnections;
	private final Set<String> activeUsers;

	public Set<String> getActiveUsers() {
		return this.activeUsers;
	}

	public WebSocketVertx() {
		this.activeConnections = new ConcurrentHashMap<>();
		this.activeUsers = ConcurrentHashMap.newKeySet();
	}

	public void handleWebSocket(final ServerWebSocket webSocket) {
		webSocket.accept();
		System.out.println("New WebSocket connection with ID: " + webSocket.path().split("/")[1]);
		this.activeConnections.put(UUID.fromString(webSocket.path().split("/")[1]), webSocket);
		this.activeUsers.add(webSocket.path().split("/")[2]);

		System.out.println("New WebSocket connection with : " + webSocket.path());
		System.out.println("New WebSocket connection from user: " + webSocket.path().split("/")[2]);
		System.out.println("Active users: " + this.activeUsers.toString());
		// this.getUsersToUpdate(webSocket);
		webSocket.handler(buffer -> {
			// Handle incoming message
			final String message = buffer.toString();
			System.out.println("Received message from " + webSocket.path().split("/")[1] + ": " + message);

			// Echo the message back to the client
			// webSocket.writeTextMessage("Echo: " + message);
		});

		webSocket.closeHandler(v -> {
			// activeConnections.remove(connectionId);
			this.activeUsers.remove(webSocket.path().split("/")[2]);
			System.out.println("WebSocket connection closed with ID: " + webSocket.path().split("/")[1]);
		});

	}

	public void sendMessageToClient(final UUID clientID, final String message) {
		System.out.println("Sending message to client: " + clientID.toString());
		final ServerWebSocket webSocket = this.activeConnections.get(clientID);
		if (webSocket != null) {
			// for (final ServerWebSocket serverWebSocket : webSocket) {
			webSocket.writeTextMessage(message);
			// }
		} else {
			System.out.println("No active connection with ID: " + clientID.toString());
		}
	}

	public void broadcastToEveryone(final String message) {
		System.out.println("Sending message to everyone: ");
		for (final ServerWebSocket webSocket : this.activeConnections.values()) {
			if (webSocket != null) {
				// for (final ServerWebSocket serverWebSocket : webSocket) {
				webSocket.writeTextMessage(message);
				// }
			} else {
				System.out.println("No active connection with ID: " + webSocket.path().toString());
			}
		}
	}

}
