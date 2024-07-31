package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.socket.emitter.Emitter;
import io.socket.socketio.server.SocketIoNamespace;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(final String[] args) {
		final VertxOptions options = new VertxOptions().setBlockedThreadCheckInterval(300_000);
		final Vertx vertx = Vertx.vertx(options);

		vertx.deployVerticle(new AppServer(), serverResult -> {
			if (serverResult.succeeded()) {
				LOGGER.debug("AppServer deployed successfully");
				LOGGER.info("AppServer deployed successfully");
				System.out.println("AppServer deployed successfully");
			} else {
				serverResult.cause().printStackTrace();
			}
		});

        final SocketIO serverWrapper = new SocketIO("127.0.0.1", 9092, null); // null means "allow all" as stated in https://github.com/socketio/engine.io-server-java/blob/f8cd8fc96f5ee1a027d9b8d9748523e2f9a14d2a/engine.io-server/src/main/java/io/socket/engineio/server/EngineIoServerOptions.java#L26
        try {
            serverWrapper.startServer();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        final SocketIoServer server = serverWrapper.getSocketIoServer();
        final SocketIoNamespace ns = server.namespace("/");
        ns.on("connection", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                final SocketIoSocket socket = (SocketIoSocket) args[0];
                System.out.println("Client " + socket.getId() + " (" + socket.getInitialHeaders().get("remote_addr") + ") has connected.");

                socket.on("new-message", new Emitter.Listener() {
                    @Override
                    public void call(final Object... args) {
                        System.out.println("[Client " + socket.getId() + "] " + args);
                        socket.send("message", "test message", 1);
                    }
                });
                
            }
        });
	}
}
