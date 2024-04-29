package generator;

import io.swagger.v3.oas.models.OpenAPI;
import io.vertx.ext.web.Router;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author ckaratza Exposes the OpenAPI spec as a vertx route.
 */
public final class OpenApiRoutePublisher {

	private static final Map<String, OpenAPI> generatedSpecs = new HashMap<>();

	public static synchronized OpenAPI publishOpenApiSpec(final Router router, final String path, final String title,
			final String version, final String serverUrl) {
		Optional<OpenAPI> spec = Optional.empty();
		if (generatedSpecs.get(path) == null) {
			final OpenAPI openAPI = OpenApiSpecGenerator.generateOpenApiSpecFromRouter(router, title, version,
					serverUrl);
			generatedSpecs.put(path, openAPI);
			spec = Optional.of(openAPI);
		}
		if (spec.isPresent()) {
			final Optional<OpenAPI> finalSpec = spec;
			return finalSpec.get();
		} else {
			return new OpenAPI();
		}
	}
}
