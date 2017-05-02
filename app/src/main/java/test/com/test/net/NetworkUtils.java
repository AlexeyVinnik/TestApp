package test.com.test.net;


import android.content.Context;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.VolleyError;

import test.com.test.R;
import test.com.test.util.Logger;

public class NetworkUtils {
    public static final String INVALID_RESPONSE = "invalid_response";

    public static void showError(Context context, VolleyError error) {
        String message = getErrorMessage(context, error);

        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        Logger.log_e(message, error);
    }

    public static String getErrorMessage(Context context, VolleyError error) {
        String message = "Unknown error";
        if (error instanceof NoConnectionError) {
            message = context.getString(R.string.error_bad_connection);
        } else if (error.getMessage().equals(NetworkUtils.INVALID_RESPONSE)) {
            message = context.getString(R.string.error_invalid_response);
        } else if (error.getMessage() != null){
            message = error.getMessage();
        }

        return message;
    }
}
