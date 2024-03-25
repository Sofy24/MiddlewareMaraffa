package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

import game.Team;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import userModule.UserService;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class UserTestIntegration {

    //TODO  login and register 
    private Vertx vertx;
    private UserService userService;

    @BeforeAll
    public void setUp() {
        this.vertx = Vertx.vertx();
        this.userService = new UserService(vertx);
    }

    /**
     * This method, called after our test, just cleanup everything by closing the
     * vert.x instance
     */
    @AfterAll
    public void tearDown() {
        vertx.close();
    }


    @Timeout(value = 10, unit = TimeUnit.MINUTES)
    @Test
    public void testRegisterEvent(VertxTestContext context){
            this.userService.registerUser("user1", "password", "email@gmail.com").whenComplete((res, err) -> {
                assertEquals(res.getString("nickname"), "user1");;
                context.completeNow();
                //Otherwise timeout will be triggered to fail the test
            });
    }

    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    @Test
    public void testRegisterFailFuture(VertxTestContext context){
            this.userService.registerUser("duplicateUser", "password", "email@gmail.com").whenComplete((res, err) -> {
                //Otherwise timeout will be triggered to fail the test
            }).join();
        
            try {
            this.userService.registerUser("duplicateUser", "password", "email@gmail.com").whenComplete((res, err) -> {
                //Otherwise timeout will be triggered to fail the test
            }).join();
            } catch (RuntimeException e) {
                context.completeNow();
            }
    }

    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    @Test
    public void testAfterGameHandler(VertxTestContext context){
        Team team1 = new Team(List.of("user1", "user2"), "teamA", 8);
        Team team2 = new Team(List.of("user3", "user4"), "teamB", 3);
        JsonObject requestBody = new JsonObject()
            .put("team1", team1)
            .put("team2", team2);
        /** testing only the necessary part of the after round body */
        this.userService.endGameHandler(requestBody).whenComplete((res, err) -> {
            if(res) context.completeNow();
            //Otherwise timeout will be triggered to fail the test
        });
    }
}
