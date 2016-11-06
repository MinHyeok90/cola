package com.example.android.cola;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.R.attr.author;
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
    String participants;

    public Album(String created_at, Map<String, Object> filelist, String isRecording, String name, String owner,String participants) {
        this.created_at = created_at;
        this.filelist = filelist;
        this.isRecording = isRecording;
        this.name = name;
        this.owner = owner;
        this.participants = participants;
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

    public String getParticipants() {
        return participants;
    }

    public void setParticipants(String participants) {
        this.participants = participants;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("created_at", created_at);
        result.put("filelist", filelist);
        result.put("isRecording", isRecording);
        result.put("name", name);
        result.put("owner", owner);
        result.put("participants", owner);

        return result;
    }
}