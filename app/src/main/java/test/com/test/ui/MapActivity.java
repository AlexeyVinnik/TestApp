package test.com.test.ui;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import test.com.test.R;
import test.com.test.database.BaseStorage;
import test.com.test.database.VehicleStorage;
import test.com.test.model.Vehicle;
import test.com.test.model.VehicleLocation;
import test.com.test.model.VehicleLocations;
import test.com.test.model.route.Route;
import test.com.test.model.route.Routes;
import test.com.test.net.GsonRequest;
import test.com.test.net.NetAPI;
import test.com.test.net.NetworkUtils;
import test.com.test.net.URLs;
import test.com.test.ui.adapters.MarkerAdapter;
import test.com.test.util.Logger;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, EasyPermissions.PermissionCallbacks {

    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";
    private static final String UPDATE_LOCATION_TAG = "update_location_tag";

    private static final int RC_APP_PERM = 202;
    private static final int UPDATE_VEHICLE_LOC_DELAY = 60 * 1000;
    private static final int VEHICLE_LOC_EXPIRE_TIME = 30 * 1000;
    private static final int UPDATE_My_LOC_DELAY = 10 * 1000;
    private static final String ROUTE_REQUEST_TAG = "ROUTE_REQUEST_TAG";

    private View mProgressBar;
    private VehicleStorage mVehicleStorage;

    private GoogleMap mMap;
    private LocListener mLocListener;
    private LocationManager mLocationManager;
    private Polyline mRouteLine;

    private int mUserID;

    private ArrayList<Vehicle> mVehicles = new ArrayList<>();
    private ArrayList<Marker> mMarkers = new ArrayList<>();

    private Handler mHandler = new Handler();

    private GsonRequest mVehicleLocationRequest = null;

    private GetVehicleLocationsAsyncTask mGetVehicleLocationsAsyncTask;
    private SaveVehLocationsAsyncTask mSaveVehicleLocationsAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Bundle extras = getIntent().getExtras();
        if (extras == null || !extras.containsKey(EXTRA_USER_ID)) {
            Logger.log(getString(R.string.error_unknown_user_id));
            onBackPressed();

            return;
        }

        mUserID = extras.getInt(EXTRA_USER_ID);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        buildVehicleLocationRequest(mUserID);

        mVehicleStorage = BaseStorage.getInstance(MapActivity.this).getVehicleStorage();
        mVehicles = new ArrayList<>();

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocListener = new LocListener();

        mProgressBar = findViewById(R.id.progress_bar);

        if (requestPermissions()) {
            mProgressBar.setVisibility(View.VISIBLE);
            requestLocationUpdates();
        }
    }

    private void buildVehicleLocationRequest(int userId) {
        mVehicleLocationRequest = new GsonRequest<>(URLs.getLocationsURL(userId),
                Request.Method.GET, VehicleLocations.class,
                new Response.Listener<VehicleLocations>() {
                    @Override
                    public void onResponse(final VehicleLocations response) {
                        initiateSaveVehLocTask(MapActivity.this, response.data);

//                        mHandler.postDelayed(mUpdatePosRunnable, UPDATE_VEHICLE_LOC_DELAY);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Logger.log_e(NetworkUtils.getErrorMessage(MapActivity.this, error), error);
                        mHandler.postDelayed(mUpdatePosRunnable, UPDATE_VEHICLE_LOC_DELAY);

                        if (mMarkers.size() == 0) {
                            Toast.makeText(MapActivity.this, R.string.error_unknown_vehicle_locations, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void requestLocationUpdates() {
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_My_LOC_DELAY, 0, mLocListener);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_My_LOC_DELAY, 0, mLocListener);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mProgressBar.setVisibility(View.GONE);

        if (mGetVehicleLocationsAsyncTask != null) {
            mGetVehicleLocationsAsyncTask.cancel(false);
        }
        mGetVehicleLocationsAsyncTask = new GetVehicleLocationsAsyncTask(MapActivity.this, false);
        mGetVehicleLocationsAsyncTask.execute();
    }


    private MarkerAdapter.IRouteOnMap mRouteOnMap = new MarkerAdapter.IRouteOnMap() {
        @Override
        public void buildRoute(LatLng vehicleLocation) {
            LatLng myLocation = mLocListener.getCoordinates();
            if (myLocation == null) {
                String errorMsg = getString(R.string.error_unknown_device_location);
                Logger.log(errorMsg);
                return;
            }

            NetAPI.getInstance(MapActivity.this).getRequestQueue().cancelAll(ROUTE_REQUEST_TAG);

            String origin = myLocation.latitude + "," + myLocation.longitude;
            String dest = vehicleLocation.latitude + "," + vehicleLocation.longitude;
            String key = getString(R.string.google_direction_key);

            String url = URLs.getRouteUrl(origin, dest, key);
            Logger.log("request url: " + url);
            GsonRequest stringRequest = new GsonRequest<>(url, Request.Method.GET, Routes.class,
                    new Response.Listener<Routes>() {
                        @Override
                        public void onResponse(Routes response) {
                            Logger.log("Routes size: " + response.routes.size());

                            drawRoute(response.getShortRoute());
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Logger.log_e("Error route loading", error);
                        }
                    });
            stringRequest.setTag(ROUTE_REQUEST_TAG);
            NetAPI.getInstance(MapActivity.this).addToRequestQueue(stringRequest);
        }
    };

    private void drawRoute(Route route) {
        if (route == null) {
            return;
        }

        PolylineOptions polylineOptions = new PolylineOptions();

        for (LatLng latLng : route.getPoints()) {
            polylineOptions.add(latLng);
        }

        if (mRouteLine != null) {
            mRouteLine.remove();
            mRouteLine = null;
        }
        mRouteLine = mMap.addPolyline(polylineOptions);
    }

    private void removeMarkers() {
        for (Marker marker : mMarkers) {
            marker.remove();
        }

        mMarkers.clear();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Logger.log("OnPermissionsGranted: " + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Logger.log("onPermissionsDenied: " + requestCode + ":" + perms.size());

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this, getString(R.string.rationale_ask_again))
                    .setTitle(getString(R.string.title_settings_dialog))
                    .setPositiveButton(getString(R.string.setting))
                    .setNegativeButton(getString(R.string.cancel), null)
                    .build()
                    .show();
        }
    }

    @AfterPermissionGranted(RC_APP_PERM)
    private boolean requestPermissions() {
        String[] perms = {
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        if (EasyPermissions.hasPermissions(this, perms)) {

            return true;

        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.permissions_required),
                    RC_APP_PERM, perms);
            return false;
        }
    }

    public class LocListener implements LocationListener {

        private static final int ACCURACY_THRESHOLD = 1000;

        public LatLng mCoordinates;

        @Override
        public void onLocationChanged(Location location) {
            if (location.getAccuracy() < ACCURACY_THRESHOLD) { //updating user's mCoordinates
                mCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public LatLng getCoordinates() {
            if (mCoordinates == null) {
                Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null) {
                    location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }

                if (location != null) {
                    mCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
                }
            }

            return mCoordinates;
        }
    }

    @Override
    public void onDestroy() {
        mVehicleStorage = null;
        mHandler.removeCallbacks(mUpdatePosRunnable);
        NetAPI.getInstance(this).getRequestQueue().cancelAll(UPDATE_LOCATION_TAG);

        Logger.log("Vehicle location update stopped");

        super.onDestroy();
    }

    private class SaveVehLocationsAsyncTask extends AsyncTask<Void, Void, Void> {

        private Context mCtx;
        private ArrayList<VehicleLocation> mVehicleLocations;

        public SaveVehLocationsAsyncTask(Context ctx, ArrayList<VehicleLocation> locations) {
            mCtx = ctx;
            mVehicleLocations = locations;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            int updatedLocations = 0;

            for (VehicleLocation location : mVehicleLocations) {
                if (location.vehicleid > 0) {
                    if (mVehicleStorage != null) {
                        mVehicleStorage.updateVehicleLocation(location.vehicleid, location.lat, location.lon);
                        updatedLocations++;
                    }
                }
            }

            Logger.log("Updated vehicle locations: " + updatedLocations);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            initiateGetVehLocTask(mCtx, true);
        }
    }

    private class GetVehicleLocationsAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private Context mContext;
        private ArrayList<Vehicle> mVehicleList;

        private boolean mIsServerChecked;
        private String mErrorMessage;

        public GetVehicleLocationsAsyncTask(Context context, boolean IsServerChecked) {
            mContext = context;
            mVehicleList = new ArrayList<>();
            mIsServerChecked = IsServerChecked;
            mErrorMessage = mContext.getString(R.string.error_unknown);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (mContext == null || mVehicleStorage == null) {
                return false;
            }

            mVehicleList = mVehicleStorage.getUserVehicles(mUserID);
            Logger.log("UserID: " + mUserID + ", stored vehicle count: " + mVehicleList.size());

            if (mVehicleList.size() == 0) {
                return false;
            }

            long now = new Date().getTime();
            Date lastUpdate = mVehicleList.get(0).lastUpdate;

            if (lastUpdate == null) {
                mErrorMessage = mContext.getString(R.string.error_unknown_vehicle_locations);

                Logger.log(mErrorMessage);
                return false;

            } else if (now - lastUpdate.getTime() > VEHICLE_LOC_EXPIRE_TIME) {
                Logger.log("The positions of vehicles are expired");
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            // when there are not valid markers
            if (!aBoolean) {
                if (mContext != null && mHandler != null) {

                    if (mIsServerChecked) {
                        Toast.makeText(mContext, mErrorMessage, Toast.LENGTH_SHORT).show();
                        mHandler.postDelayed(mUpdatePosRunnable, UPDATE_VEHICLE_LOC_DELAY);
                    } else {
                        mHandler.post(mUpdatePosRunnable);
                    }

                } else {
                    Logger.log("Can not initiate updates for vehicles");
                }

                return;

            } else if (mContext != null && mMap != null && mHandler != null) {

                removeMarkers();
                mVehicles = mVehicleList;

                Marker marker;
                LatLng latLng = null;
                for (Vehicle vehicle : mVehicles) {
                    latLng = new LatLng(vehicle.latitude, vehicle.longitude);
                    marker = mMap.addMarker(new MarkerOptions().position(latLng));
                    mMarkers.add(marker);
                }

                if (latLng != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 9));
                }

                mMap.setInfoWindowAdapter(new MarkerAdapter(MapActivity.this.getLayoutInflater(),
                        mVehicles, mContext, mRouteOnMap));

                mHandler.postDelayed(mUpdatePosRunnable, UPDATE_VEHICLE_LOC_DELAY);

            } else {
                Logger.log("Can not initiate updates for vehicles");
            }
        }
    }

    private void initiateGetVehLocTask(Context context, boolean isServerChecked) {
        if (mGetVehicleLocationsAsyncTask != null) {
            mGetVehicleLocationsAsyncTask.cancel(false);
        }
        if (context != null) {
            mGetVehicleLocationsAsyncTask = new GetVehicleLocationsAsyncTask(context, isServerChecked);
            mGetVehicleLocationsAsyncTask.execute();
        }
    }

    private void initiateSaveVehLocTask(Context context, ArrayList<VehicleLocation> locations) {
        if (mSaveVehicleLocationsAsyncTask != null) {
            mSaveVehicleLocationsAsyncTask.cancel(false);
        }
        if (context != null) {
            mSaveVehicleLocationsAsyncTask = new SaveVehLocationsAsyncTask(context, locations);
            mSaveVehicleLocationsAsyncTask.execute();
        }
    }

    private Runnable mUpdatePosRunnable = new Runnable() {
        @Override
        public void run() {
            Logger.log("Update vehicles locations request was initiated");

            mVehicleLocationRequest.setTag(UPDATE_LOCATION_TAG);
            NetAPI.getInstance(getApplicationContext()).addToRequestQueue(mVehicleLocationRequest);
        }
    };
}
