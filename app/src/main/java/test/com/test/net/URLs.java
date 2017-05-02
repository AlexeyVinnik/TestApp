package test.com.test.net;


public class URLs {

    private static final String BASE_URL = "http://mobi.connectedcar360.net/api/";

    private static final String GET_USER_LIST = BASE_URL + "?op=list";
    private static final String GET_LOCATIONS = BASE_URL + "?op=getlocations&userid=";

    private static final String GET_ROUTE = "https://maps.googleapis.com/maps/api/directions/json?";

    public static String getUserListURL() {
        return GET_USER_LIST;
    }

    public static String getLocationsURL(int userId) {
        return GET_LOCATIONS + userId;
    }

    public static String getRouteUrl(String origin, String dest, String key) {
        return GET_ROUTE + "origin=" + origin + "&destination=" + dest + "&key=" + key;
    }
}
