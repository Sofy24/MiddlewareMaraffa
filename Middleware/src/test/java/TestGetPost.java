import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.example.UserAPI;
import org.example.experimentsGetPost.UserVerticle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class TestGetPost {

    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(UserVerticle.class.getName(), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testGetUser(TestContext context) {
        final Async async = context.async();
        vertx.createHttpClient().getNow(8080, "localhost", "/users/123", response -> {
            response.handler(body -> {
                context.assertEquals("User ID: 123", body.toString());
                async.complete();
            });
        });
    }

    /*@Test
    public void testCreateUser(TestContext context) {
        final Async async = context.async();
        vertx.createHttpClient().post(8080, "localhost", "/users", response -> {
            response.handler(body -> {
                context.assertEquals("User created successfully!", body.toString());
                async.complete();
            });
        }).end();
    }*/
}
