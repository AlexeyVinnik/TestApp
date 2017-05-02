package test.com.test.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import test.com.test.R;
import test.com.test.model.Vehicle;
import test.com.test.net.NetAPI;
import test.com.test.util.Logger;

public class MarkerAdapter implements GoogleMap.InfoWindowAdapter {
    private LayoutInflater mInflater = null;
    private ArrayList<Vehicle> mVehicles;
    private Context mContext;
    private Marker mMarkerWindow;

    private IRouteOnMap mRouteOnMap;

    public MarkerAdapter(LayoutInflater inflater, ArrayList<Vehicle> vehicles,
                         Context context, IRouteOnMap routeOnMap) {
        mInflater = inflater;
        mVehicles = (vehicles != null) ? vehicles : new ArrayList<Vehicle>();
        mContext = context.getApplicationContext();
        mRouteOnMap = routeOnMap;
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        final View popupView = mInflater.inflate(R.layout.marker_tooltip, null);
        popupView.setOnClickListener(null);
        mMarkerWindow = marker;

        Vehicle vehicle = null;
        final LatLng markerPos = marker.getPosition();
        for (Vehicle item : mVehicles) {
            if (item.latitude == markerPos.latitude && item.longitude == markerPos.longitude) {
                vehicle = item;
                break;
            }
        }

        if (vehicle != null) {

            TextView title = (TextView) popupView.findViewById(R.id.title);
            title.setText(vehicle.make + " " + vehicle.model);

            View colorView = popupView.findViewById(R.id.snippet);
            int color = Color.TRANSPARENT;
            try {
                color = Color.parseColor(vehicle.color);
            } catch (Exception e) {
                Logger.log_e("Unknown color: " + vehicle.color, e);
            }
            colorView.setBackgroundColor(color);

            // It is necessary to set image at once,
            // because we can not change any views after rendering of info window
            ImageView vehicleImage = ((ImageView) popupView.findViewById(R.id.vehicle_icon));
            Bitmap bitmap = NetAPI.getInstance(mContext).getBitmap(vehicle.foto);
            if (bitmap != null) {
                vehicleImage.setImageBitmap(bitmap);
            } else {
                vehicleImage.setImageResource(R.mipmap.ic_launcher);
                final String photoUrl = vehicle.foto;
                ImageRequest request = new ImageRequest(photoUrl,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap response) {
                                if (response != null) {
                                    NetAPI.getInstance(mContext).putBitmap(photoUrl, response);

                                    LatLng markerPosition = mMarkerWindow.getPosition();
                                    if (markerPos.equals(markerPosition)) {
                                        mMarkerWindow.hideInfoWindow();
                                        mMarkerWindow.showInfoWindow();
                                    }
                                }
                            }
                        }, 128, 0, ImageView.ScaleType.CENTER_CROP, null,
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Logger.log_e("Error image loading", error);
                            }
                        });
                NetAPI.getInstance(mContext).addToRequestQueue(request);
            }

            String address = "";
            Geocoder geocoder = new Geocoder(mContext);
            try {
                List<Address> addrList = geocoder.getFromLocation(vehicle.latitude, vehicle.longitude, 2);
                if (addrList.size() > 0) {

                    for (Address addr : addrList) {
                        if (!TextUtils.isEmpty(addr.getAddressLine(0))) {
                            address = addrList.get(0).getAddressLine(0);
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            TextView addressView = (TextView) popupView.findViewById(R.id.address);
            addressView.setText(address);

            if (mRouteOnMap != null) {
                mRouteOnMap.buildRoute(markerPos);
            }

        } else {
            Logger.log("The marker for current vehicle not found");
        }

        return popupView;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        return null;
    }

    public interface IRouteOnMap {
        public void buildRoute(LatLng vehicleLocation);
    }
}