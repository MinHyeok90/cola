package com.example.android.cola;

import android.provider.BaseColumns;

/**
 * Created by KM on 2016. 11. 7..
 */

public final class Databases {
    // DataBase Table
    public static final class CreateDB implements BaseColumns {
        public static final String EMAIL = "email";
        public static final String TIMESTAMP = "timestamp";
        public static final String _TABLENAME = "FRIENDSLOG";

        public static final String _CREATE =
                "create table "+_TABLENAME+"("
                        +_ID+" integer primary key autoincrement, "
                        +TIMESTAMP+" text not null , "
                        +EMAIL+" text not null );";
    }
}
