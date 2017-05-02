package test.com.test.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import test.com.test.R;
import test.com.test.database.BaseStorage;
import test.com.test.database.UserStorage;
import test.com.test.database.VehicleStorage;
import test.com.test.model.UserData;
import test.com.test.model.UserInfo;
import test.com.test.model.Vehicle;
import test.com.test.net.GsonRequest;
import test.com.test.net.NetAPI;
import test.com.test.net.NetworkUtils;
import test.com.test.net.URLs;
import test.com.test.ui.adapters.UserListAdapter;
import test.com.test.util.Logger;
import test.com.test.util.PrefUtils;
import test.com.test.util.Utils;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, View.OnClickListener {
    private static final int RC_APP_PERM = 201;

    private View mPermissionMessageView;
    private View mProgressBar;
    private Button mBtnRefresh;

    private UserListAdapter mAdapter;
    private String[] mPermission;

    private SaveUsersAsyncTask mDBSaveTask;
    private GetUsersAsyncTask mDBLoadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPermission = new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        initViews();

        if (requestPermissions()) {
            getUsersData();
        }
    }

    private void initViews() {
        mProgressBar = findViewById(R.id.progress_bar);

        RecyclerView list = (RecyclerView) findViewById(R.id.user_list);
        list.setHasFixedSize(true);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(mLayoutManager);

        DividerItemDecoration decoration = new DividerItemDecoration(this, mLayoutManager.getOrientation());
        list.addItemDecoration(decoration);

        mAdapter = new UserListAdapter(null, this);
        list.setAdapter(mAdapter);

        mPermissionMessageView = findViewById(R.id.view_permission_msg);
        Button btnSettings = (Button) findViewById(R.id.btn_request_permissions);
        btnSettings.setOnClickListener(this);

        mBtnRefresh = (Button) findViewById(R.id.btn_refresh_list);
        mBtnRefresh.setOnClickListener(this);
    }

    private void getUsersData() {
        Date today = Utils.getDateFromString(Utils.getStringDate(new Date())); // to get date without any count of hours
        Date last = PrefUtils.getUsersLoadingDate(this);

        if (last == null || last.before(today)) {
            Logger.log("User list is expired. Trying to update from the server.");

            GsonRequest getUserRequest = new GsonRequest<>(URLs.getUserListURL(), Request.Method.GET, UserData.class,
                    new Response.Listener<UserData>() {
                        @Override
                        public void onResponse(UserData response) {
                            PrefUtils.saveUsersLoadingTime(MainActivity.this, new Date());

                            cancelSaveTask(false);

                            for (UserInfo userInfo : response.data) {
                                if (userInfo.userid < 1) {
                                    response.data.remove(userInfo);
                                }
                            }

                            mDBSaveTask = new SaveUsersAsyncTask(MainActivity.this, response.data);
                            mDBSaveTask.execute();

                            mAdapter.setDataSet(response.data);
                            mProgressBar.setVisibility(View.GONE);
                            showEmptyViewIfNeed();

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mProgressBar.setVisibility(View.GONE);
                            NetworkUtils.showError(MainActivity.this, error);

                            showEmptyViewIfNeed();
                        }
                    });

            NetAPI.getInstance(this).addToRequestQueue(getUserRequest);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            cancelLoadTask(false);

            mDBLoadTask = new GetUsersAsyncTask(MainActivity.this);
            mDBLoadTask.execute();
        }
    }

    private void showEmptyViewIfNeed() {
        if (mAdapter.getItemCount() > 0) {
            mBtnRefresh.setVisibility(View.GONE);
        } else {
            mBtnRefresh.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        cancelLoadTask(false);
        cancelSaveTask(false);

        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Logger.log("onPermissionsGranted: " + requestCode + ":" + perms.size());

        if (requestCode == RC_APP_PERM && mPermission.length == perms.size()) {
            mPermissionMessageView.setVisibility(View.GONE);
            getUsersData();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Logger.log("onPermissionsDenied: " + requestCode + ":" + perms.size());
        mPermissionMessageView.setVisibility(View.VISIBLE);
    }

    private boolean requestPermissions() {
        if (EasyPermissions.hasPermissions(this, mPermission)) {
            mPermissionMessageView.setVisibility(View.GONE);
            return true;

        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.permissions_required),
                    RC_APP_PERM, mPermission);
            mPermissionMessageView.setVisibility(View.VISIBLE);
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_APP_PERM) {
            if (requestPermissions()) {
                getUsersData();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_request_permissions: {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, RC_APP_PERM);
            }
            break;
            case R.id.btn_refresh_list:
                getUsersData();
                break;
            default:
                break;
        }
    }

    private class SaveUsersAsyncTask extends AsyncTask<Void, Void, Void> {

        private Context mCtx;
        private ArrayList<UserInfo> mUserList;

        public SaveUsersAsyncTask(Context ctx, ArrayList<UserInfo> userList) {
            mCtx = ctx;
            mUserList = userList;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            if (mCtx != null) {
                UserStorage userStorage = BaseStorage.getInstance(mCtx).getUserStorage();
                VehicleStorage vehicleStorage = BaseStorage.getInstance(mCtx).getVehicleStorage();

                int updatedUserCount = 0;
                for (UserInfo userInfo : mUserList) {
                    userStorage.addUser(userInfo);

                    for (Vehicle vehicle : userInfo.vehicles) {
                        if (vehicle.vehicleid > 0) {
                            vehicleStorage.addVehicle(vehicle, userInfo.userid);
                        }
                    }

                    updatedUserCount++;
                }
                Logger.log("Updated user count: " + updatedUserCount);
            } else {
                Logger.log("Error with saving users to database: Context is null");
            }
            return null;
        }
    }

    private class GetUsersAsyncTask extends AsyncTask<Void, Void, Void> {

        private ArrayList<UserInfo> mUserList;
        private Context mCtx;

        public GetUsersAsyncTask(Context ctx) {
            mCtx = ctx;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (this.mCtx != null && mProgressBar != null) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mCtx != null) {
                mUserList = BaseStorage.getInstance(mCtx).getUserStorage().getAllUserInfo();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (this.mCtx != null && mProgressBar != null && mAdapter != null) {
                mAdapter.setDataSet(mUserList);

                mProgressBar.setVisibility(View.GONE);

                showEmptyViewIfNeed();
            } else {
                Logger.log("Error with getting users from database: Context is null.");
            }
        }
    }

    private void cancelSaveTask(boolean interrupt) {
        if (mDBSaveTask != null) {
            mDBSaveTask.cancel(interrupt);
            mDBSaveTask = null;
        }
    }

    private void cancelLoadTask(boolean interrupt) {
        if (mDBLoadTask != null) {
            mDBLoadTask.cancel(interrupt);
            mDBLoadTask = null;
        }
    }
}
