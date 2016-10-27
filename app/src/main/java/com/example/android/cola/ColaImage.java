package com.example.android.cola;

/**
 * Created by Kimminhyeok on 2016. 10. 27..
 */

public class ColaImage {
    String filename;
    String url;

    public ColaImage(String filename, String url) {
        this.filename = filename;
        this.url = url;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
