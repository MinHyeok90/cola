package com.example.android.cola;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MakeAndInviteActivity extends AppCompatActivity {
    public final String TAG = "MakeAndInviteActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_and_invite);

        // ActionBar에 타이틀 변경
        getSupportActionBar().setTitle("앨범앨범");
        Intent intent = getIntent();

        List<Group> groups = intent.getParcelableArrayListExtra("groups");
        //Map<String>

        final List<GroupItem> groupItems = new ArrayList<>();
        final List<String> emailList = new ArrayList<>();

        for(Group g : groups){
            for(String s : g.getNameList()){
                if(! emailList.contains(s)){
                    emailList.add(s);
                }
            }
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    String dbemail = messageSnapshot.child("email").getValue().toString();
                    if(emailList.contains(dbemail)){
                        FriendListItem friendListItem = new FriendListItem();

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public class GroupItem {
        private List<FriendListItem> friends;
        private Group group;

        public GroupItem(List<FriendListItem> friends, Group group) {
            this.friends = friends;
            this.group = group;
        }

        public List<FriendListItem> getFriends() {
            return friends;
        }

        public void setFriends(List<FriendListItem> friends) {
            this.friends = friends;
        }

        public Group getGroup() {
            return group;
        }

        public void setGroup(Group group) {
            this.group = group;
        }
    }
}
