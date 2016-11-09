package com.example.android.cola;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MakeAndInviteActivity extends AppCompatActivity {
    public final String TAG = "MakeAndInviteActivity";
    final Map<String, User> mEmailKeyMap = new HashMap<>();
    ListViewAdapter mListViewAdapter;
    FirebaseUser mUser;
    DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_and_invite);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference("albumtest");

        // ActionBar에 타이틀 변경
        getSupportActionBar().setTitle("앨범앨범");
        Intent intent = getIntent();

        final List<Group> groups = intent.getParcelableArrayListExtra("groups");


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
                    String dbemail = (String) messageSnapshot.child("mEmail").getValue();
                    for(String e : emailList){
                        if(e.matches(dbemail)){
                            String uid = messageSnapshot.child("mUid").getValue().toString();
                            String userName = messageSnapshot.child("mUserName").getValue().toString();
                            String photoUrl = messageSnapshot.child("mPhotoUrl").getValue().toString();

                            mEmailKeyMap.put(dbemail, new User(uid,dbemail,userName,photoUrl));
                        }
                    }
                }
                mListViewAdapter = new ListViewAdapter(getApplicationContext(), R.layout.friendsrecommenditem, groups );
                ListView listView = (ListView)findViewById(R.id.inviteListView);
                listView.setAdapter(mListViewAdapter);
                //mListViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private class ListViewAdapter extends ArrayAdapter<Group> {
        private List<Group> items;

        public ListViewAdapter(Context context, int textViewResourceId, List<Group> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        public class ViewHolder {
            //public ImageView ivPic;
            public TextView tvTitle;
            public Button btnCreate;
            /*public TextView tvPower;
            public TextView tvSkill;*/
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MakeAndInviteActivity.ListViewAdapter.ViewHolder viewHolder;

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.friendsrecommenditem, parent, false);

                viewHolder = new MakeAndInviteActivity.ListViewAdapter.ViewHolder();
                viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.rec_title);
                viewHolder.btnCreate = (Button) convertView.findViewById(R.id.rec_createAlbumBtn);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (MakeAndInviteActivity.ListViewAdapter.ViewHolder) convertView.getTag();
            }
            final List<String> emailList = items.get(position).getNameList();
            final Long startTime = Long.parseLong(items.get(position).getStartTime());
            final Date date = new Date(startTime);
            viewHolder.tvTitle.setText(date.toString()+"부터 찍은 사진");
            viewHolder.btnCreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분");

                    final String dateString = sdfNow.format(date);
                    Album newAlbum = new Album(String.valueOf(startTime), null, "True", dateString+"부터 찍은 사진", mUser.getUid(), null);
                    DatabaseReference r = mRef.push();
                    r.setValue(newAlbum);
                    for(String s : emailList){
                        String uid = mEmailKeyMap.get(s).getmUid();
                        r.child("participants").child(uid).setValue(s);
                    }
                    Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);

                    intent.putExtra("albumKey", r.getKey());
                    intent.putExtra("albumName", dateString+"부터 찍은 사진");
                    intent.putExtra("albumDate", String.valueOf(startTime));
                    intent.putExtra("albumOwner", mUser.getUid());

                    startActivity(intent);
                }
            });

            for(int i=0; i<emailList.size(); i++){

                User user = mEmailKeyMap.get(emailList.get(i));
                if (user != null) {

                    LinearLayout ll = (LinearLayout) convertView.findViewById(R.id.rec_linearLayout);

                    ImageView imageView = new ImageView(convertView.getContext());
                    LinearLayout.LayoutParams imageViewLayoutParams = new LinearLayout.LayoutParams(80,80);
                    imageView.setLayoutParams(imageViewLayoutParams);

                    TextView textView = new TextView(convertView.getContext());


                    Glide.with(convertView.getContext())
                            .load(user.getmPhotoUrl())
                            .into(imageView);
                    textView.setText(user.getmUserName());

                    ll.addView(textView);
                    ll.addView(imageView);

                }

            }



            return convertView;
        }

    }

}
