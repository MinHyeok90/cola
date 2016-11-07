package com.example.android.cola;

import java.io.Serializable;

/**
 * Created by Krivnon on 2016-09-25.
 */
class User implements Serializable{
    private String mUid;
    private String mEmail;
    private String mUserName;
    private String mPhotoUrl;


    User() {
    }

    public User(String mUid, String mEmail, String mUserName, String mPhotoUrl) {
        this.mUid = mUid;
        this.mEmail = mEmail;
        this.mUserName = mUserName;
        this.mPhotoUrl = mPhotoUrl;
    }

    public String getmUid() {
        return mUid;
    }

    public void setmUid(String mUid) {
        this.mUid = mUid;
    }

    public String getmEmail() {
        return mEmail;
    }

    public void setmEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getmUserName() {
        return mUserName;
    }

    public void setmUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public String getmPhotoUrl() {
        return mPhotoUrl;
    }

    public void setmPhotoUrl(String mPhotoUrl) {
        this.mPhotoUrl = mPhotoUrl;
    }
}