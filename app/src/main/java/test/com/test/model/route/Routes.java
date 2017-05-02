package test.com.test.model.route;

import java.util.ArrayList;

public class Routes {
    public ArrayList<Route> routes;

    public Route getShortRoute() {
        Route result = null;
        if (routes != null && routes.size() > 0) {
            result = routes.get(0);
            int minDistance = result.getDistance();
            for (Route route : routes) {
                int distance = route.getDistance();
                if (distance < minDistance) {
                    result = route;
                    minDistance = result.getDistance();
                }
            }
        }
        return result;
    }
}
