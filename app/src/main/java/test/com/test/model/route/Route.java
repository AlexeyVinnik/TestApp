package test.com.test.model.route;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Route {
    public ArrayList<Leg> legs;

    public int getDistance() {
        int distance = 0;
        for (Leg leg : legs) {
            distance += leg.distance.value;
        }

        return distance;
    }

    public ArrayList<LatLng> getPoints() {
        ArrayList<LatLng> points = new ArrayList<>();
        for (Leg leg : legs) {
            points.addAll(leg.getPoints());
        }

        return points;
    }
}
