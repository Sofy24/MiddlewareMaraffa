package org.example.httpRest;

import java.lang.reflect.Field;
import java.util.*;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.apache.commons.lang3.reflect.FieldUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

import java.lang.reflect.Type;

import generator.OpenApiRoutePublisher;
import generator.Required;
import io.swagger.v3.oas.models.OpenAPI;
import io.vertx.ext.web.Router;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.Schema;
import org.example.service.GameServiceDecorator;
import org.example.utils.Constants;

public class RouterConfig {
    private static final String APPLICATION_JSON = "application/json";
    private final int port;
    private final Controller controller;

    public RouterConfig(final int port, final GameServiceDecorator entityService) {
        this.port = port;
        this.controller = new Controller(entityService);
    }

    private void mapParameters(Field field, Map<String, Object> map) {
        Class<?> type = field.getType();
        Class<?> componentType = field.getType().getComponentType();

        if (isPrimitiveOrWrapper(type)) {
            Schema primitiveSchema = new Schema();
            primitiveSchema.type(field.getType().getSimpleName());
            map.put(field.getName(), primitiveSchema);
        } else {
            HashMap<String, Object> subMap = new HashMap<String, Object>();

            if (isPrimitiveOrWrapper(componentType)) {
                HashMap<String, Object> arrayMap = new HashMap<String, Object>();
                arrayMap.put("type", componentType.getSimpleName() + "[]");
                subMap.put("type", arrayMap);
            } else {
                subMap.put("$ref", "#/components/schemas/" + componentType.getSimpleName());
            }

            map.put(field.getName(), subMap);
        }
    }

    private Boolean isPrimitiveOrWrapper(Type type) {
        return type.equals(Double.class) ||
                type.equals(Float.class) ||
                type.equals(Long.class) ||
                type.equals(Integer.class) ||
                type.equals(Short.class) ||
                type.equals(Character.class) ||
                type.equals(Byte.class) ||
                type.equals(Boolean.class) ||
                type.equals(UUID.class) ||
                type.equals(String.class);
    }

    public Router configurationRouter() {
        Vertx vertx = Vertx.vertx();
        Router router = Router.router(vertx);
        router.route().consumes(APPLICATION_JSON);
        router.route().produces(APPLICATION_JSON);
        router.route().handler(BodyHandler.create());

        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("auth");
        allowedHeaders.add("Content-Type");

        Set<HttpMethod> allowedMethods = new HashSet<>();
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

        for(IRouteResponse route : controller.getRoutes()){
            router.route(route.getMethod(), route.getRoute()).handler(route.getHandler());
        }
        
        OpenAPI openAPIDoc = OpenApiRoutePublisher.publishOpenApiSpec(
                router,
                "spec",
                "Vertx Swagger Auto Generation",
                "1.0.0",
                "http://localhost:" + port + "/");

        /*
         * Tagging section. This is where we can group end point operations; The tag
         * name is then used in the end point annotation
         */
        openAPIDoc
                .addTagsItem(new io.swagger.v3.oas.models.tags.Tag().name(Constants.GAME_TAG).description("Game operations"))
                .addTagsItem(new io.swagger.v3.oas.models.tags.Tag().name(Constants.ROUND_TAG).description("Round operations"));

        // Generate the SCHEMA section of Swagger, using the definitions in the Model
        // folder
        ImmutableSet<ClassPath.ClassInfo> modelClasses = getClassesInPackage("org.example.service");

        Map<String, Object> map = new HashMap<String, Object>();

        for (ClassPath.ClassInfo modelClass : modelClasses) {
            Field[] fields = FieldUtils.getFieldsListWithAnnotation(modelClass.load(), Required.class)
                    .toArray(new Field[0]);
            List<String> requiredParameters = new ArrayList<String>();

            for (Field requiredField : fields) {
                requiredParameters.add(requiredField.getName());
            }

            fields = modelClass.load().getDeclaredFields();
            
            for (Field field : fields) {
                if(field.getType() != null &&  field.getType().getComponentType() != null) mapParameters(field, map);
            }

            openAPIDoc.schema(modelClass.getSimpleName(),
                    new Schema()
                            .title(modelClass.getSimpleName())
                            .type("object")
                            .required(requiredParameters)
                            .properties(map));

            map = new HashMap<String, Object>();
        }

        // Serve the Swagger JSON spec out on /swagger
        router.get("/swagger").handler(res -> {
            res.response()
                    .setStatusCode(200)
                    .end(Json.pretty(openAPIDoc));
        });

        // Serve the Swagger UI out on /doc/index.html
        router.route("/doc/*").handler(
                StaticHandler.create().setCachingEnabled(false).setWebRoot("webroot/node_modules/swagger-ui-dist"));

        return router;
    }

    public ImmutableSet<ClassPath.ClassInfo> getClassesInPackage(String pckgname) {
        try {
            ClassPath classPath = ClassPath.from(Thread.currentThread().getContextClassLoader());
            return classPath.getTopLevelClasses(pckgname);
        } catch (Exception e) {
            return null;
        }
    }

}
