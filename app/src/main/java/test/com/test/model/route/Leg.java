package test.com.test.model.route;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Leg {
    public ArrayList<Step> steps;
    public Distance distance;

    public ArrayList<LatLng> getPoints() {
        ArrayList<LatLng> latLngs = new ArrayList<>();
        for (Step step : steps) {
            latLngs.addAll(step.getPoints());
        }

        return latLngs;
    }
}
