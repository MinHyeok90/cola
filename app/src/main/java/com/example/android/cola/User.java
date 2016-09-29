package com.example.android.cola;

/**
 * Created by Krivnon on 2016-09-25.
 */
class User {
    private String Uid;
    private String Email;

    User() {
    }

    User(String m_Uid, String m_Email) {
        Uid = m_Uid;
        Email = m_Email;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

}