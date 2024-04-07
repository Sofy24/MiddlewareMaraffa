package integration;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import chatModule.ChatService;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class ChatIntegration {

    private Vertx vertx;
    private ChatService chatService;

    @BeforeAll
    public void setUp() {
        this.vertx = Vertx.vertx();
        this.chatService = new ChatService(this.vertx);
    }

    /**
     * This method, called after our test, just cleanup everything by closing the
     * vert.x instance
     */
    @AfterAll
    public void tearDown() {
        this.vertx.close();
    }

    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    @Test
    public void testCreateGameChatHandler(final VertxTestContext context) {
        this.chatService.createGameChatHandler("genericGameID").whenComplete((res, err) -> {
            context.verify(() -> {
                assertNull(res.getString("error"));
                context.completeNow();
            });
            // Otherwise timeout will be triggered to fail the test
        });
    }

}
