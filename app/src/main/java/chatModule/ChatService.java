package chatModule;

import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.impl.logging.Logger;

public class ChatService {
    private Vertx vertx;
    private static final int PORT = 3005;
    private static final String LOCALHOST = "127.0.0.1";
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class); 


    public ChatService(Vertx vertx) {
        this.vertx = vertx;
    }
    

    public void messageReceived(String msg){
        LOGGER.info("Received message: " + msg);
        System.out.println(msg);
    }
}
