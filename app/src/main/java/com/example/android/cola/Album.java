package com.example.android.cola;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.R.id.list;

/**
 * Created by Kimminhyeok on 2016. 10. 27..
 */


public class Album {
    String created_at;
    Map<String, Object> filelist;
    String isRecording;
    String name;
    String owner;

    public Album(String created_at, Map<String, Object> filelist, String isRecording, String name, String owner) {
        this.created_at = created_at;
        this.filelist = filelist;
        this.isRecording = isRecording;
        this.name = name;
        this.owner = owner;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public Map<String, Object> getFilelist() {
        return filelist;
    }

    public void setFilelist(Map<String, Object> filelist) {
        this.filelist = filelist;
    }

    public String getIsRecording() {
        return isRecording;
    }

    public void setIsRecording(String isRecording) {
        this.isRecording = isRecording;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}