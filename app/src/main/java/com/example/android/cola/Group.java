package com.example.android.cola;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KM on 2016. 11. 9..
 */

public class Group implements Parcelable{
    private String startTime;
    private String endTime;
    private List<String> nameList;

    public Group(String startTime, String endTime, List<String> nameList) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.nameList = nameList;
    }
    protected Group(Parcel in) {
        startTime = in.readString();
        endTime = in.readString();
        if (in.readByte() == 0x01) {
            nameList = new ArrayList<String>();
            in.readList(nameList, String.class.getClassLoader());
        } else {
            nameList = null;
        }
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(startTime);
        dest.writeString(endTime);
        if (nameList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(nameList);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Group> CREATOR = new Parcelable.Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public List<String> getNameList() {
        return nameList;
    }

    public void setNameList(List<String> nameList) {
        this.nameList = nameList;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }


}