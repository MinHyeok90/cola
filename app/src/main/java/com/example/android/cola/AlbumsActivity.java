package com.example.android.cola;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import static android.R.attr.data;
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
 * Modify by 김민혁 on 2016-10-24
 *  앨범 선택하면 GalleryActivity로 연결(DB 연동X).
 *  DB연동을 위해 클래스 ImageAdapter_main를 inner class로 변경
 *  받은 정보를 이용해, 갤러리의 첫번째 그림을 썸네일로 출력함
 *
 * Modify by 김민혁 on 2016-10-27
 *  album 제목, 날짜 출력.
 *  제목 지정해서 새로운 앨범 생성기능 추가.
 *  update, delete 기능 추가하려다가 갤러리에서 추가하기로 함. -> 관련코드 주석처리.
 *
 * Modify by 민경태 on 2016-10-27
 * 로그아웃 기능 추가
 *
 * Modify by 김미래 on 2016-11-02
 * 로그아웃 기능 구현
 *
 * Modify by 김민혁 on 2016-11-03
 * 새 앨범 생성시 owner 올바르게 지정
 *
 * Modified by 김미래 on 2016. 11. 04.
 *  ColaImage 멤버 owner 추가 사항 반영
 *
 * Modified by 김민혁 on 2016. 11. 05.
 *  git merge하며 owner기능 구현 위한 mUser 통합
 */

public class AlbumsActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    /* Modify by 김민혁 on 2016-10-24 */
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mRef = mDatabase.getReference("albumtest");   //DB에서 Albumtest 명칭 변경시, 변경 필요
    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

    public GridView mGridView;
    public GridAdapter mGridAdapter;
    public final String TAG = "AlbumActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        Intent it = getIntent();

        final List albumKeyList = new ArrayList();
        final List albumNameList = new ArrayList();
        final List albumDateList = new ArrayList();
        final List thumbnailUrls = new ArrayList();

        mGridView = (GridView)findViewById(R.id.gridview);
        mGridAdapter = new GridAdapter(getApplicationContext(), R.layout.albums_thumbnail, thumbnailUrls, albumNameList,albumDateList);
        mGridView.setAdapter(mGridAdapter);  // 커스텀 아답타를 GridView 에 적용// GridView 항목의 레이아웃 row.xml

        //앨범 클릭시 동작
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(AlbumsActivity.this, i + "번째 그림 선택",
                        Toast.LENGTH_SHORT).show();

                //GalleryActivity로 연결(DB 연동X)
                Intent intent = new Intent(AlbumsActivity.this, GalleryActivity.class);
                intent.putExtra("albumKey",albumKeyList.get(i).toString());
                intent.putExtra("albumName",albumNameList.get(i).toString());
                intent.putExtra("albumDate",albumDateList.get(i).toString());
                startActivity(intent);
            }
        });

        //앨범 list 가져오기
        mRef.addValueEventListener(
            new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    albumKeyList.clear();
                    albumNameList.clear();
                    albumDateList.clear();
                    thumbnailUrls.clear();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        //String idx = child.getKey().toString();
                        if(child != null) {
                            String albumKey = child.getKey().toString();
                            String albumName = child.child("name").getValue().toString();
                            String albumDate = child.child("created_at").getValue().toString();
                            String albumImgUrl = child.child("filelist").child("1").child("url").getValue().toString();
                            albumKeyList.add(albumKey);
                            albumNameList.add(albumName);
                            albumDateList.add(albumDate);
                            thumbnailUrls.add(albumImgUrl);
                            Log.d(TAG, "albumkey : "+albumKey);
                        }
                    }
                    mGridAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    // ...
                }
            });

    }

    /* 메뉴 기능 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_album, menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }

    /* 메뉴 선택시 동작. 메뉴의 ID를 case로 구분 */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_album:
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
                        /* 앨범생성 test용 데이터생성 */
                        Map<String,Object> albumhash = new HashMap<String, Object>();
                        Map<String,Object> filelisthash = new HashMap<String, Object>();
                        String uid = mUser.getUid();
                        ColaImage c = new ColaImage("newFile","https://firebasestorage.googleapis.com/v0/b/cola-b6336.appspot.com/o/1%2FP60920-223052.jpg?alt=media&token=8941d7a6-dcf7-417d-a81c-869fd937f465", mUser.getUid());
                        filelisthash.put("1",c);

                        /* 저장 시작 */
                        Long date = new Date().getTime();
//                        setContentView(R.layout.dialog_new_album_layout);
//                        EditText et = (EditText)bld.findViewById(R.id.new_album_title_edit_text); //다른 layout에 있는 경우 id에 의한 탐색시 무조건 null이 반환됨.
                        Album newAlbum = new Album(date.toString(),filelisthash,"True",input.getText().toString(),mUser.getUid());
                        DatabaseReference r = mRef.push();
                        r.setValue(newAlbum);
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

            Glide.with(getApplicationContext())
                    .load(getItem(i))
                    .centerCrop()
                    .override(256,256)
                    .error(R.drawable.ic_action_name)
                    .into(imageView);

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
