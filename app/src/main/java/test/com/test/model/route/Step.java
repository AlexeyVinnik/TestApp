package test.com.test.model.route;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.List;

public class Step {
    public Polyline polyline;

    public List<LatLng> getPoints(){
        return PolyUtil.decode(polyline.points);
    }
}
