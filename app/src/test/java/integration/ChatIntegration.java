package integration;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import chatModule.ChatService;
import game.service.User;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class ChatIntegration {

    private final static User TEST_USER = new User("testUser", UUID.randomUUID());
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

    private CompletableFuture<JsonObject> createAGame() {
        return this.chatService.createGameChatHandler("genericGameID");
    }

    private CompletableFuture<JsonObject> joinTheGame(final String gameID, final User user) {
        return this.chatService.joinGameHandler(gameID, user);
    }

    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    @Test
    public void testCreateGameChatHandler(final VertxTestContext context) {
        this.createAGame().whenComplete((res, err) -> {
            context.verify(() -> {
                assertNull(res.getString("error"));
                context.completeNow();
            });
        });
    }

    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    @Test
    public void testJoin(final VertxTestContext context) {
        this.createAGame().join();
        for (int i = 0; i < 3; i++) {
            this.joinTheGame("genericGameID", new User(TEST_USER.username() + i, TEST_USER.clientID())).whenComplete((res, err) -> {
                context.verify(() -> {
                    assertNull(res.getString("error"));
                });
            });
        }
        context.completeNow();
    }

}
