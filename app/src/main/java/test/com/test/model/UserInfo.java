package test.com.test.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class UserInfo {

    @SerializedName("userid")
    public int userId;

    @SerializedName("owner")
    public User owner;

    @SerializedName("vehicles")
    public ArrayList<Vehicle> vehicles;
}
