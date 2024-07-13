package httpRest;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

import chatModule.ChatController;
import game.service.GameServiceDecorator;
import game.utils.Constants;
import generator.OpenApiRoutePublisher;
import generator.Required;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.tags.Tag;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;
import userModule.UserController;

/*
 * This class is responsible for defining the router of the application containing all the endopoints and the Swagger configuration.
 */
public class RouterConfig {
	private static final String APPLICATION_JSON = "application/json";
	private final int port;
	private final Controller controller;

	public RouterConfig(final int port, final GameServiceDecorator entityService, final UserController userController,
			final ChatController chatController) {
		this.port = port;
		this.controller = new Controller(entityService, userController, chatController);
	}

	private void mapParameters(final Field field, final Map<String, Object> map) {
		final Class<?> type = field.getType();
		final Class<?> componentType = field.getType().getComponentType();

		if (this.isPrimitiveOrWrapper(type)) {
			final Schema primitiveSchema = new Schema();
			primitiveSchema.type(field.getType().getSimpleName());
			map.put(field.getName(), primitiveSchema);
		} else {
			final HashMap<String, Object> subMap = new HashMap<String, Object>();

			if (this.isPrimitiveOrWrapper(componentType)) {
				final HashMap<String, Object> arrayMap = new HashMap<String, Object>();
				arrayMap.put("type", componentType.getSimpleName() + "[]");
				subMap.put("type", arrayMap);
			} else {
				subMap.put("$ref", "#/components/schemas/" + componentType.getSimpleName());
			}

			map.put(field.getName(), subMap);
		}
	}

	private Boolean isPrimitiveOrWrapper(final Type type) {
		return type.equals(Double.class) || type.equals(Float.class) || type.equals(Long.class)
				|| type.equals(Integer.class) || type.equals(Short.class) || type.equals(Character.class)
				|| type.equals(Byte.class) || type.equals(Boolean.class) || type.equals(UUID.class)
				|| type.equals(String.class);
	}

	public Router configurationRouter(final Vertx vertx) {
		final Router router = Router.router(vertx);
		router.route().consumes(APPLICATION_JSON);
		router.route().produces(APPLICATION_JSON);
		router.route().handler(BodyHandler.create());

		final Set<String> allowedHeaders = new HashSet<>();
		allowedHeaders.add("auth");
		allowedHeaders.add("Content-Type");

		final Set<HttpMethod> allowedMethods = new HashSet<>();
		allowedMethods.add(HttpMethod.GET);
		allowedMethods.add(HttpMethod.POST);
		allowedMethods.add(HttpMethod.OPTIONS);
		allowedMethods.add(HttpMethod.DELETE);
		allowedMethods.add(HttpMethod.PATCH);
		allowedMethods.add(HttpMethod.PUT);

		router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));

		router.route().handler(context -> {
			context.response().headers().add(CONTENT_TYPE, APPLICATION_JSON);
			context.next();
		});
		router.route().failureHandler(ErrorHandler.create(vertx, true));

		for (final IRouteResponse route : this.controller.getRoutes()) {
			router.route(route.getMethod(), route.getRoute()).handler(route.getHandler());
		}

		final OpenAPI openAPIDoc = OpenApiRoutePublisher.publishOpenApiSpec(router, "spec",
				"Vertx Swagger Auto Generation", "1.0.0", "http://localhost:" + this.port + "/");

		/*
		 * Tagging section. This is where we can group end point operations; The tag
		 * name is then used in the end point annotation
		 */
		openAPIDoc
				.addTagsItem(
						new Tag().name(Constants.GAME_TAG).description("Game operations"))
				.addTagsItem(new Tag().name(Constants.ROUND_TAG)
						.description("Round operations"));

		// Generate the SCHEMA section of Swagger, using the definitions in the Model
		// folder
		final ImmutableSet<ClassPath.ClassInfo> modelClasses = ImmutableSet.<ClassPath.ClassInfo>builder()
				.addAll(this.getClassesInPackage("game"))
				.addAll(this.getClassesInPackage("userModule"))
				.addAll(this.getClassesInPackage("BLManagement"))
				.addAll(this.getClassesInPackage("chatModule"))
				.build();

		Map<String, Object> map = new HashMap<String, Object>();

		for (final ClassPath.ClassInfo modelClass : modelClasses) {
			Field[] fields = FieldUtils.getFieldsListWithAnnotation(modelClass.load(), Required.class)
					.toArray(new Field[0]);
			final List<String> requiredParameters = new ArrayList<String>();

			for (final Field requiredField : fields) {
				requiredParameters.add(requiredField.getName());
			}

			fields = modelClass.load().getDeclaredFields();

			for (final Field field : fields) {
				if (field.getType() != null && field.getType().getComponentType() != null) {
					this.mapParameters(field, map);
				}
			}

			openAPIDoc.schema(modelClass.getSimpleName(), new Schema().title(modelClass.getSimpleName()).type("object")
					.required(requiredParameters).properties(map));

			map = new HashMap<String, Object>();
		}

		// Serve the Swagger JSON spec out on /swagger
		router.get("/swagger").handler(res -> {
			res.response().setStatusCode(200).end(Json.pretty(openAPIDoc));
		});

		// Serve the Swagger UI out on /doc/index.html
		router.route("/doc/*").handler(
				StaticHandler.create().setCachingEnabled(false).setWebRoot("webroot/node_modules/swagger-ui-dist"));

		return router;
	}

	public ImmutableSet<ClassPath.ClassInfo> getClassesInPackage(final String pckgname) {
		try {
			final ClassPath classPath = ClassPath.from(Thread.currentThread().getContextClassLoader());
			return classPath.getTopLevelClasses(pckgname);
		} catch (final Exception e) {
			return null;
		}
	}
}
