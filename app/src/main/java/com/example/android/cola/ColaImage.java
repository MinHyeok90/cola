package com.example.android.cola;

/**
 * Created by Kimminhyeok on 2016. 10. 27..
 *
 * Modified by 김미래 on 2016. 11. 04.
 *  멤버 owner 추가 : 누가 올린 사진인지 알 수 있게
 */

public class ColaImage {
    String filename;
    String url;
    String owner;

    public ColaImage(String filename, String url, String owner) {
        this.filename = filename;
        this.url = url;
        this.owner = owner;
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
