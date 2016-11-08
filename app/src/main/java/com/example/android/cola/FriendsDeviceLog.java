package com.example.android.cola;

/**
 * Created by KM on 2016. 11. 7..
 */

public class FriendsDeviceLog {
    String UserID;
    String timestamp;

    public FriendsDeviceLog(String userID, String timestamp) {
        this.UserID = userID;
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        this.UserID = userID;
    }
}
