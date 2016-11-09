package com.example.android.cola;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.drm.DrmManagerClient;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.android.cola.R.id.gridview;
/*
 * Created by 김민혁 on 2016-09-15
 *  앨범집 activity.
 *  클래스 생성 : ImageAdapter
 *  이미지 클릭시 Toast 메시지 출력
 *
 * Modify by 김민혁 on 2016-09-17
 *  새로운 앨범 생성시 newAlbum으로 intent.
 *
 * Modify by 김민혁 on 2016-09-25
 *  newAlbum으로 intent 삭제
 *  class명 충돌로 인해 이름변경 : ImageAdapter -> ImageAdapter_main
 *  액션바에 메뉴 생성 + 코드 정리.
 *
 * Modifyed by 김민혁 on 2016-10-24
 *  앨범 선택하면 GalleryActivity로 연결(DB 연동X).
 *  DB연동을 위해 클래스 ImageAdapter_main를 inner class로 변경
 *  받은 정보를 이용해, 갤러리의 첫번째 그림을 썸네일로 출력함
 *
 * Modifyed by 김민혁 on 2016-10-27
 *  album 제목, 날짜 출력.
 *  제목 지정해서 새로운 앨범 생성기능 추가.
 *  update, delete 기능 추가하려다가 갤러리에서 추가하기로 함. -> 관련코드 주석처리.
 *
 * Modifyed by 민경태 on 2016-10-27
 * 로그아웃 기능 추가
 *
 * Modifyed by 김미래 on 2016-11-02
 * 로그아웃 기능 구현
 *
 * Modifyed by 김민혁 on 2016-11-03
 * 새 앨범 생성시 owner 올바르게 지정
 *
 * Modified by 김미래 on 2016-11-04
 *  ColaImage 멤버 owner 추가 사항 반영
 *
 * Modified by 김민혁 on 2016-11-05
 *  git merge하며 owner기능 구현 위한 mUser 통합
 *
 * Modified by 민경태 on 2016-11-06
 * 자신이 참여한 앨범인지 확인 후 앨범들을 보여줌.
 * 자신의 앨범만 볼 수 있음
 *
 * Modified by 김민혁 on 2016-11-08
 *  처음으로 앨범 생성 시, 임시 파일을 넣지 말고 아직 이미지 없음을 출력.
 *  no image는 내부 drawable에 있는 no_picture를 사용하도록 수정
 *
 */

public class AlbumsActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    /* Modify by 김민혁 on 2016-10-24 */
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;  //DB에서 Albumtest 명칭 변경시, 변경 필요
    FirebaseUser mUser;

    public GridView mGridView;
    public GridAdapter mGridAdapter;
    public final String TAG = "AlbumActivity";
    private Boolean isMine = false;
    private Button btnrefresh;


    final List albumKeyList = new ArrayList();
    final List albumNameList = new ArrayList();
    final List albumDateList = new ArrayList();
    final List thumbnailUrls = new ArrayList();
    final List albumOwnerList = new ArrayList();


    private String albumKey;
    private String albumName;
    private String albumDate;
    private String albumImgUrl;
    private String albumOwner;

    private String albumParty;
    private String ownUid;

    //private OnValueEventHandler mHandler = null;
    private Iterable<DataSnapshot> snapshots = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mHandler = new OnValueEventHandler();
       //getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setContentView(R.layout.activity_albums);
        Intent it = getIntent();
        try {
            mUser = FirebaseAuth.getInstance().getCurrentUser();
            Log.d(TAG, "mUser : "+mUser.getEmail());
        } catch (Exception e) {
            if(mUser!=null)
            Log.d(TAG, "mUser : null"+mUser.getEmail());

        }
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference("albumtest");

        mGridView = (GridView)findViewById(R.id.gridview);
        mGridAdapter = new GridAdapter(getApplicationContext(), R.layout.albums_thumbnail, thumbnailUrls, albumNameList,albumDateList);
        mGridView.setAdapter(mGridAdapter);  // 커스텀 아답타를 GridView 에 적용// GridView 항목의 레이아웃 row.xml

        btnrefresh = (Button)findViewById(R.id.album_btrefresh);
        //앨범 클릭시 동작
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //GalleryActivity로 연결
                Intent intent = new Intent(AlbumsActivity.this, GalleryActivity.class);
                intent.putExtra("albumKey",albumKeyList.get(i).toString());
                intent.putExtra("albumName",albumNameList.get(i).toString());
                intent.putExtra("albumDate",albumDateList.get(i).toString());
                intent.putExtra("albumOwner",albumOwnerList.get(i).toString());
                startActivity(intent);
            }
        });
        //mRef.addValueEventListener(mHandler);
        //getAlbumList();
        getAlbumList();
        if (albumKeyList.size() == 0){
            GridView gv = (GridView) findViewById(R.id.gridview);
//            gv.setBackground();
        }
    }

    public void getAlbumList()
    {
        //앨범 list 가져오기
        mRef.addValueEventListener(
            new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    albumKeyList.clear();
                    albumNameList.clear();
                    albumDateList.clear();
                    thumbnailUrls.clear();
                    albumOwnerList.clear();
                    //FireBase의 앨범 추가
                    //앨범키 / 이름 / 날짜 / URL 정보를 가져오고
                    //앨범 참여자 중에 자신이 있는지 비교한 후 add함
                    //참여자가 아닌 앨범 Owner가 자신이라면 역시 add함
                    isMine = false;

                    mUser = FirebaseAuth.getInstance().getCurrentUser();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        albumParty = "";
                        if (child != null) {
                            albumKey = child.getKey().toString();
                            albumName = child.child("name").getValue().toString();
                            albumDate = child.child("created_at").getValue().toString();
                            albumImgUrl = child.child("thumbnail").getValue().toString();
                            albumOwner = child.child("owner").getValue().toString();

                            if(mUser==null){ownUid= "";}
                            else{
                                ownUid = mUser.getUid();
                                albumParty = "";
                                if (child.child("participants").child(mUser.getUid()).getValue() != null){
                                    albumParty = child.child("participants").child(ownUid).getValue().toString();
                                    //Log.w(TAG,"key : "+albumParty);
                                }
                                if (child.child("participants").child(mUser.getUid()).getKey() != null) {
                                    //Log.w(TAG, "keyChild : " + child.child("participants").child(mUser.getUid()).getKey().toString());
                                    if (albumParty.equals(mUser.getEmail().toString())) {
                                        //Log.w(TAG, "keyUid : " + mUser.getEmail().toString());
                                        //Log.w(TAG, "key : " + albumParty);
                                        albumKeyList.add(albumKey);
                                        albumNameList.add(albumName);
                                        albumDateList.add(albumDate);
                                        thumbnailUrls.add(albumImgUrl);
                                        //만일 이미지가 없다면, no_picture 이미지를 출력한다.(아답터에서 no_picture이미지 할당)

                                        albumOwnerList.add(albumOwner);
                                        //Log.w(TAG, "albumName : " + albumName);
                                    }
                                }
                            }

                        }
                    }

                    mGridAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG,"mRef.addValueEventListener: onCancelled");
                }
            }
        );
    }


    public void onClick(View view)
    {
        isMine = false;
        if(view.getId() == R.id.album_btrefresh)
        {
            //Log.d(TAG, "동기화버튼 클릭");
            mUser = FirebaseAuth.getInstance().getCurrentUser();
            //getAlbumList();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //앨범 list 가져오기
        mRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        albumKeyList.clear();
                        albumNameList.clear();
                        albumDateList.clear();
                        thumbnailUrls.clear();
                        albumOwnerList.clear();
                        //FireBase의 앨범 추가
                        //앨범키 / 이름 / 날짜 / URL 정보를 가져오고
                        //앨범 참여자 중에 자신이 있는지 비교한 후 add함
                        //참여자가 아닌 앨범 Owner가 자신이라면 역시 add함
                        isMine = false;

                        mUser = FirebaseAuth.getInstance().getCurrentUser();
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            albumParty = "";
                            if (child != null) {
                                albumKey = child.getKey().toString();
                                albumName = child.child("name").getValue().toString();
                                albumDate = child.child("created_at").getValue().toString();
                                albumImgUrl = child.child("thumbnail").getValue().toString();
                                albumOwner = child.child("owner").getValue().toString();

                                if(mUser==null){ownUid= "";}
                                else{
                                    ownUid = mUser.getUid();
                                    albumParty = "";
                                    if (child.child("participants").child(mUser.getUid()).getValue() != null){
                                        albumParty = child.child("participants").child(ownUid).getValue().toString();
                                        //Log.w(TAG,"key : "+albumParty);
                                    }
                                    if (child.child("participants").child(mUser.getUid()).getKey() != null) {
                                        //Log.w(TAG, "keyChild : " + child.child("participants").child(mUser.getUid()).getKey().toString());
                                        if (albumParty.equals(mUser.getEmail().toString())) {
                                            //Log.w(TAG, "keyUid : " + mUser.getEmail().toString());
                                            //Log.w(TAG, "key : " + albumParty);
                                            albumKeyList.add(albumKey);
                                            albumNameList.add(albumName);
                                            albumDateList.add(albumDate);
                                            thumbnailUrls.add(albumImgUrl);
                                            //만일 이미지가 없다면, no_picture 이미지를 출력한다.(아답터에서 no_picture이미지 할당)

                                            albumOwnerList.add(albumOwner);
                                            //Log.w(TAG, "albumName : " + albumName);
                                        }
                                    }
                                }

                            }
                        }

                        mGridAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG,"mRef.addValueEventListener: onCancelled");
                    }
                }
        );
        //getAlbumList();
        mGridAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGridAdapter.notifyDataSetChanged();
    }

    /* 메뉴 기능 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_album, menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        return super.onCreateOptionsMenu(menu);
    }

    /* 메뉴 선택시 동작. 메뉴의 ID를 case로 구분 */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_album:
                mUser = FirebaseAuth.getInstance().getCurrentUser();
                /* 추가 버튼 클릭시, 대화상자 출력 */
                AlertDialog.Builder bld = new AlertDialog.Builder(this);
                bld.setTitle("새 앨범 추가");
                final EditText input = new EditText(this);
                input.setHint("새로운 앨범 제목을 작성해주세요.");
//                bld.setView(R.layout.dialog_new_album_layout);    //다른 layout을 사용하는 경우, 해당 xml에 들어있는 EditText의 Text를 읽어오지 못해서 일단 EditText를 코드로 삽입.
                bld.setView(input);
                bld.setIcon(R.drawable.imoticon1);
                bld.setPositiveButton("생성",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        /* 저장 시작 */
                        Long date = new Date().getTime();
                        // newAlbum 매개변수 : String created_at, Map<String, Object> filelist, String isRecording, String name, String owner, String thumbnail
                        Album newAlbum = new Album(date.toString(), null, "True", input.getText().toString(), mUser.getUid(), null);
                        DatabaseReference r = mRef.push();
                        r.setValue(newAlbum);
                        r.child("participants").child(mUser.getUid()).setValue(mUser.getEmail());

                        //생성된 GalleryActivity로 연결
                        Intent intent = new Intent(AlbumsActivity.this, GalleryActivity.class);
                        intent.putExtra("albumKey",r.getKey().toString());
                        intent.putExtra("albumName",input.getText().toString());
                        intent.putExtra("albumDate",date.toString());
                        intent.putExtra("albumOwner",mUser.getUid());
                        startActivity(intent);
                    }
                });
                bld.setNegativeButton("취소",null);
                bld.show();

                return true;

//            case R.id.action_edit_album:
//                return true;

            case R.id.action_logout:

                signOut();
                return true;
            case R.id.action_bluetoothTest:
                Intent intent = new Intent(AlbumsActivity.this, BluetoothTestActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    //로그아웃
    private void signOut() {

        Intent intent = new Intent(this, LoginActivity.class);
        FirebaseAuth.getInstance().signOut();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            /*user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AlbumsActivity.this, "Your profile is deleted:( Create a account now!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AlbumsActivity.this, "Failed to delete your account!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });*/
        }
        else{
            //success
        }
        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    /*
     * Created by 김민혁 on 2016-09-15
     *  클래스 생성 : ImageAdapter
     *
     * Modify by 김민혁 on 2016-09-25
     *  class명 충돌로 인해 이름변경 : ImageAdapter -> ImageAdapter_main
     *
     * Modify by 김민혁 on 2016-10-24
     *  GalleryActivity내 GridAdapter로 전체 변경
     *
     */
    class GridAdapter extends BaseAdapter {
        //리스트 layout을 위한 변수 3개
        Context context;
        int layout;
        LayoutInflater layoutInflater;
        List thumnailList;     //정보 받아올 list
        List titleList;     //정보 받아올 list
        List dateList;     //정보 받아올 list

        public GridAdapter(Context context, int layout, List thumnailList, List nameList, List dateList){
            this.context = context;
            this.layout = layout;
            this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.thumnailList = thumnailList;
            this.titleList = nameList;
            this.dateList = dateList;
        }

        @Override
        public int getCount() {
            return thumnailList.size();
        }

        @Override
        public Object getItem(int i) { return thumnailList.get(i); }
        public Object getTitle(int i) { return titleList.get(i); }
        public Object getDate(int i) { return dateList.get(i); }

        @Override
        public long getItemId(int i) {
            return i;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view==null)
                view = layoutInflater.inflate(layout, null);
            // Put it in the image view

            final ImageView imageView = (ImageView) view.findViewById(R.id.albumThumbnailImage);
            final long MAX_BYTE = 1024;

            if (i % 2 == 0) {
                Glide.with(getApplicationContext())
                        .load(getItem(i))
                        .centerCrop()
                        .override(256, 256)
                        .error(R.drawable.dummyalbum1)
                        .into(imageView);
            }
            else {
                Glide.with(getApplicationContext())
                        .load(getItem(i))
                        .centerCrop()
                        .override(256, 256)
                        .error(R.drawable.dummyalbum2)
                        .into(imageView);
            }

            final TextView titleView = (TextView) view.findViewById(R.id.albumThumbnailTitle);
            titleView.setText(getTitle(i).toString());
            final TextView dateView = (TextView) view.findViewById(R.id.albumThumbnailDate);
            Date date = new Date(Long.parseLong(getDate(i).toString()));
            SimpleDateFormat format = new SimpleDateFormat("yy년 MM월 dd일");
            dateView.setText(format.format(date));

            return view;
        }
    }
}
