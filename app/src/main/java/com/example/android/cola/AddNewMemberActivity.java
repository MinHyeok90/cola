package com.example.android.cola;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * Created by kyuholee on 2016. 9. 6..
 * Modified by 경태 on 2016. 11.6
 *   앨범에 참여자 추가
 */
public class AddNewMemberActivity extends BaseActivity{
    private static final String TAG = "AllMembersActivity";

    private ListView mLvMembers;
    private ListViewAdapter mAdapter = null;
    private ArrayList<User> mUserArray; //모든 유저 - 가입된 유저
    private ArrayList<User> mPartyUser; //가입된 유저
    private DatabaseReference mDatabase;
    private FirebaseUser mUser;


    private String mAlbumkey;
    private String mMenu;

    private Button btn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnewmember);

        Intent intent = getIntent();
        mAlbumkey = intent.getStringExtra("albumkey");
        mMenu = intent.getStringExtra("menu");         //party에서 온 것인지
                                                        //invite에서 온 것인지

        mUserArray = new ArrayList<User>();
        mPartyUser = new ArrayList<User>();
        mLvMembers = (ListView) findViewById(R.id.lvMembers);
        mAdapter = new ListViewAdapter(this, R.layout.layout_member_list_item, mUserArray);
        mLvMembers.setAdapter(mAdapter);
        mLvMembers.setOnItemClickListener(mItemClickListener);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null) {
            Toast.makeText(this, "No user loggin.",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        btn = (Button)findViewById(R.id.btnMembers);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initFriend();
            }
        });
        mUserArray.clear();
        initFriend();
        mAdapter.notifyDataSetChanged();
    }

    public void initFriend()
    {

        //mDatabase.child("users").addListenerForSingleValueEvent(allMemberListener);
        //친구들 목록 ArrayList에 저장하기
        //mDatabase.child("users").addValueEventListener(allMemberListener);
        mDatabase.child("users").addChildEventListener(allMemberListener);
        //추가된 친구 안보이게 하기
        DatabaseReference mParticipants = mDatabase.child("albumtest").child(mAlbumkey).child("participants").getRef();
        //mParticipants.addListenerForSingleValueEvent(allMemberListener);
        //mParticipants.addValueEventListener(memberListener);
        mParticipants.addChildEventListener(memberListener);
        mParticipants.addValueEventListener(memberListener1);
        mAdapter.notifyDataSetChanged();

        //btn.performClick();
    }
//
    ValueEventListener memberListener1 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
//            ArrayList<User> mPartyUser = new ArrayList<User>();
//            for (DataSnapshot child : dataSnapshot.getChildren()) {
//                String partykey = child.getKey();
//                for (int i = 0; i < mUserArray.size(); i++) {
//                    if (mUserArray.get(i).getmUid().toString().equals(partykey)) {
//                        if (mMenu.equals("invite")) {
//                            //Log.i(TAG, "invite : " + mMenu);
//                            mUserArray.remove(i);
//                        } else if (mMenu.equals("party")) {
//                            mPartyUser.add(mUserArray.get(i));
//                        }
//                    }
//                }
//            }
//            mAdapter.notifyDataSetChanged();
//            //지워질 애들이 사실은 party원들
//            if(mMenu.equals("party"))
//            {
//                mUserArray.clear();
//                for(int i = 0; i < mPartyUser.size(); i++) {
//                    mUserArray.add(mPartyUser.get(i));
//                }
//            }
            //지워질 애들이 사실은 party원들
            if(mMenu.equals("party"))
            {
                mUserArray.clear();
                for(int i = 0; i < mPartyUser.size(); i++) {
                    mUserArray.add(mPartyUser.get(i));
                }
            }

            mAdapter.notifyDataSetChanged();
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "getUser:onCancelled", databaseError.toException());
        }
    };
//
//    ValueEventListener allMemberListener = new ValueEventListener() {
//        @Override
//        public void onDataChange(DataSnapshot dataSnapshot) {
//            mUserArray.clear();
//            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                User user = postSnapshot.getValue(User.class);
//                mUserArray.add(user);
//            }
//        }
//        @Override
//        public void onCancelled(DatabaseError databaseError) {
//            Log.w(TAG, "getUser:onCancelled", databaseError.toException());
//        }
//    };
    ChildEventListener memberListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String partykey = dataSnapshot.getKey();
                for (int i = 0; i < mUserArray.size(); i++) {
                    if (mUserArray.get(i).getmUid().toString().equals(partykey)) {
                        if (mMenu.equals("invite")) {
                            //Log.i(TAG, "invite : " + mMenu);
                            mUserArray.remove(i);
                        } else if (mMenu.equals("party")) {
                            mPartyUser.add(mUserArray.get(i));
                        }
                    }
                }

                mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "getUser:onCancelled", databaseError.toException());
        }
    };

    ChildEventListener allMemberListener = new ChildEventListener() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                User user = dataSnapshot.getValue(User.class);
                mUserArray.add(user);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "getUser:onCancelled", databaseError.toException());
        }
    };

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long l_position) {
            // parent는 AdapterView의 속성의 모두 사용 할 수 있다.
            //party일때는 추가하지 않음.
            if(mMenu.equals("party"))
            {

            }else
            {
                User user = (User) parent.getAdapter().getItem(position);
                Toast.makeText(getApplicationContext(),"등록"+user.getmEmail(), Toast.LENGTH_SHORT).show();
                DatabaseReference r = mDatabase.child("albumtest").child(mAlbumkey).child("participants").child(user.getmUid());
                r.setValue(user.getmEmail());
                mUserArray.remove(position);
                mAdapter.notifyDataSetChanged();
            }

        }
    };
    //@Override
    //public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

// /* 변경 시작 */
//                final DatabaseReference albumRef = myRef.child(mAlbumKey);
//                albumRef.child("name").setValue(mAlbumName);
//
//                // ActionBar에 타이틀 변경
//                getSupportActionBar().setTitle(mAlbumName);
   // }

//    @Override
//    public void onItemClick(ListView adapterView, View view, int i, long l) {
//
//        User user = mUserArray.get(i);
//        DatabaseReference r = mDatabase.child("albumtest").child(mAlbumkey).child("participants").push();
//        r.setValue(user.getmUserName());
//        Toast.makeText(getApplicationContext(),"클릭",Toast.LENGTH_LONG).show();
//// /* 변경 시작 */
////                final DatabaseReference albumRef = myRef.child(mAlbumKey);
////                albumRef.child("name").setValue(mAlbumName);
////
////                // ActionBar에 타이틀 변경
////                getSupportActionBar().setTitle(mAlbumName);
//    }

    private class ListViewAdapter extends ArrayAdapter<User> {
        private ArrayList<User> items;

        public ListViewAdapter(Context context, int textViewResourceId, ArrayList<User> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        public class ViewHolder {
            public ImageView ivPic;
            public TextView tvName;
            public TextView tvEmail;
            public TextView tvPower;
            public TextView tvSkill;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.layout_member_list_item, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.ivPic = (ImageView) convertView.findViewById(R.id.ivPic);
                viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv00);
                viewHolder.tvEmail = (TextView) convertView.findViewById(R.id.tvEmail);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            User user = items.get(position);
            if (user != null) {
                Glide.with(getApplicationContext())
                        .load(user.getmPhotoUrl())
                        .into(viewHolder.ivPic);
                viewHolder.tvName.setText(user.getmUserName());
                viewHolder.tvEmail.setText(user.getmEmail());

            }
            return convertView;
        }

    }


}