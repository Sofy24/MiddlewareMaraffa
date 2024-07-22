package generator;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.Server;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * @author ckaratza Tries to interrogate vertx router and build an OpenAPI
 *         specification. Tries to interrogate handlers of route with OpenApi
 *         Operation methods and cross-reference with route information. By no
 *         means all OpenApi 3 spec is covered, so this part will be adjusted
 *         based on use cases encountered.
 */
public final class OpenApiSpecGenerator {
	private static final Logger log = LoggerFactory.getLogger(OpenApiSpecGenerator.class);

	public static OpenAPI generateOpenApiSpecFromRouter(final Router router, final String title, final String version,
			final String serverUrl) {
		log.info("Generating Spec for vertx routes.");
		final OpenAPI openAPI = new OpenAPI();
		final Info info = new Info();
		info.setTitle(title);
		info.setVersion(version);
		final Server server = new Server();
		server.setUrl(serverUrl);
		openAPI.servers(Collections.singletonList(server));
		openAPI.setInfo(info);

		final Map<String, PathItem> paths = extractAllPaths(router);
		extractOperationInfo(router, paths);
		paths.forEach(openAPI::path);
		return openAPI;
	}

	private static Map<String, PathItem> extractAllPaths(final Router router) {
		return router.getRoutes().stream().filter(x -> x.getPath() != null).map(Route::getPath).distinct()
				.collect(Collectors.toMap(x -> x, x -> new PathItem()));
	}

	private static void extractOperationInfo(final Router router, final Map<String, PathItem> paths) {
		router.getRoutes().forEach(route -> {
			final PathItem pathItem = paths.get(route.getPath());
			if (pathItem != null) {
				final List<Operation> operations = extractOperations(route, pathItem);
				operations.forEach(operation -> operation.setParameters(extractPathParams(route.getPath())));
			}
		});
		decorateOperationsFromAnnotationsOnHandlers(router, paths);
	}

	private static void decorateOperationsFromAnnotationsOnHandlers(final Router router,
			final Map<String, PathItem> paths) {
		router.getRoutes().stream().filter(x -> x.getPath() != null).forEach(route -> {
			try {
				final Field stateF = route.getClass().getDeclaredField("state");
				stateF.setAccessible(true);
				final Field contextHandlers = stateF.get(route).getClass().getDeclaredField("contextHandlers");
				contextHandlers.setAccessible(true);
				final List<Handler<RoutingContext>> handlers = (List<Handler<RoutingContext>>) contextHandlers
						.get(stateF.get(route));
				handlers.forEach(handler -> {
					try {
						final Class<?> delegate = handler.getClass().getDeclaredField("arg$1").getType();
						Arrays.stream(delegate.getDeclaredMethods()).distinct().forEach(method -> {
							final io.swagger.v3.oas.annotations.Operation annotation = method
									.getAnnotation(io.swagger.v3.oas.annotations.Operation.class);
							if (annotation != null) {
								final String httpMethod = annotation.method();
								final PathItem pathItem = paths.get(route.getPath());
								Operation matchedOperation = null;
								switch (PathItem.HttpMethod.valueOf(httpMethod.toUpperCase())) {
									case TRACE:
										matchedOperation = pathItem.getTrace();
										break;
									case PUT:
										matchedOperation = pathItem.getPut();
										break;
									case POST:
										matchedOperation = pathItem.getPost();
										break;
									case PATCH:
										matchedOperation = pathItem.getPatch();
										break;
									case GET:
										matchedOperation = pathItem.getGet();
										break;
									case OPTIONS:
										matchedOperation = pathItem.getOptions();
										break;
									case HEAD:
										matchedOperation = pathItem.getHead();
										break;
									case DELETE:
										matchedOperation = pathItem.getDelete();
										break;
									default:
										break;
								}
								if (matchedOperation != null
										&& (annotation.operationId().equals(route.getPath().substring(1)))) {
									AnnotationMappers.decorateOperationFromAnnotation(annotation, matchedOperation);
									final RequestBody body = method.getParameters()[0].getAnnotation(RequestBody.class);
									if (body != null) {
										matchedOperation.setRequestBody(AnnotationMappers.fromRequestBody(body));
									}
								}
							}
						});
					} catch (final NoSuchFieldException e) {
						log.warn(e.getMessage());
					}
				});
			} catch (IllegalAccessException | NoSuchFieldException e) {
				log.warn(e.getMessage());
			}
		});
	}

	private static List<Parameter> extractPathParams(final String fullPath) {
		final String[] split = fullPath.split("\\/");
		return Arrays.stream(split).filter(x -> x.startsWith(":")).map(x -> {
			final Parameter param = new Parameter();
			param.name(x.substring(1));
			return param;
		}).collect(Collectors.toList());
	}

	private static List<Operation> extractOperations(final Route route, final PathItem pathItem) {
		try {
			final Field stateF = route.getClass().getDeclaredField("state");
			stateF.setAccessible(true);
			final Field methodsF = stateF.get(route).getClass().getDeclaredField("methods");
			methodsF.setAccessible(true);
			final Set<HttpMethod> httpMethods = (Set<HttpMethod>) methodsF.get(stateF.get(route));
			return httpMethods.stream().map(httpMethod -> {
				final Operation operation = new Operation();
				switch (PathItem.HttpMethod.valueOf(httpMethod.name())) {
					case TRACE:
						pathItem.trace(operation);
						break;
					case PUT:
						pathItem.put(operation);
						break;
					case POST:
						pathItem.post(operation);
						break;
					case PATCH:
						pathItem.patch(operation);
						break;
					case GET:
						pathItem.get(operation);
						break;
					case OPTIONS:
						pathItem.options(operation);
						break;
					case HEAD:
						pathItem.head(operation);
						break;
					case DELETE:
						pathItem.delete(operation);
						break;
					default:
						break;
				}
				return operation;
			}).collect(Collectors.toList());

		} catch (NoSuchFieldException | IllegalAccessException e) {
			log.warn(e.getMessage());
			return Collections.emptyList();
		}
	}

	private OpenApiSpecGenerator() {
	}
}
