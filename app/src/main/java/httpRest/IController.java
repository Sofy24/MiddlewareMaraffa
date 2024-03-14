package httpRest;

import java.util.List;

public interface IController {
    /**@return the list with all the routes*/
    List<IRouteResponse> getRoutes();
}
