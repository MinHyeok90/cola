package com.example.android.cola;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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

public class FriendListActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemClickListener{
    Button btn;
    EditText edt;
    String email;
    private FirebaseDatabase database;
    private DatabaseReference emailRef;

    ListView listView;
    ArrayList<FriendListItem> items;
    FriendListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendlist);
        database = FirebaseDatabase.getInstance();
        emailRef = database.getReference("users");
        Log.d("하이","호이");
        adapter = new FriendListAdapter(FriendListActivity.this);
        //layout의 리스트 뷰를 받아옴
        listView =(ListView)findViewById(R.id.listView);
        //adapter장착
        listView.setOnItemClickListener(this);
        listView.setAdapter(adapter);

        init();
    }
    void init()
    {
        items = new ArrayList<FriendListItem>();
        btn = (Button)findViewById(R.id.prf_btn);
        edt = (EditText)findViewById(R.id.prf_edt);
        btn.setOnClickListener(FriendListActivity.this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.prf_btn:
                email = edt.getText().toString();
                //이메일을 통해 찾기
                emailRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                            String dbemail = messageSnapshot.child("email").getValue().toString();
                            if(email.equals(dbemail))
                            {
                                Log.d("찾았습니다.",dbemail);
                                adapter.addItem("김미녁",dbemail,R.drawable.imoticon1);

//                                User myUser = new User(user.getUid(),user.getEmail());
//                                String key = myUser.getUid();
//                                //myRef.push().setValue(myUser.getUid());
//                                myRef.child(myUser.getUid()).child("email").setValue(myUser.getEmail());
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }
}
