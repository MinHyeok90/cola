package com.example.android.cola;


/**
 * Created by 민경태 on 2016-10-24.
 * Modify by 민경태 on 2016-10-24.
 * 클래스 생성 : FriendListItem
 * Adapter를 채울 정보
 *
 * Modify by 민경태 on 2016-10-24
 */

public class FriendListItem {

    private String[] name = {
            "김민혁","김미래","민경태"
    };

    private String[] msg = {
            "세상은","살기","힘드렁"
    };

    private int[] res = {
            R.drawable.imoticon1, R.drawable.imoticon1, R.drawable.imoticon1
    };

    public int getSize() {
        return name.length;
    }

    public String getName(int position) {
        return name[position];
    }

    public String getMsg(int position) {
        return msg[position];
    }

    public int imgResource(int position) {
        return res[position];
    }

}
