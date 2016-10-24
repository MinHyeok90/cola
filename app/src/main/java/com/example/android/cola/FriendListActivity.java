package com.example.android.cola;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by 민경태 on 2016-10-24.
 * Modify by 민경태 on 2016-10-24.
 * 클래스 생성 : FriendListActivity
 * Adapter를 이용한 List 출력과 친구 추가 기능 구현을 위한 Class
 *
 * Modify by 민경태 on 2016-10-24
 * 사용자의 이미지도 받아올 수 있도록 구현 예정
 * 친구 추가 기능은 아직 DB 미구현
 */

public class FriendListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendlist);

        //layout의 리스트 뷰를 받아옴
        ListView listView =(ListView)findViewById(R.id.listView);
        //adapter받아오기
        FriendListAdapter adapter = new FriendListAdapter(this);
        //adapter장착
        listView.setAdapter(adapter);
    }
}
