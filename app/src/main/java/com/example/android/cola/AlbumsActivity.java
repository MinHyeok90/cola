package com.example.android.cola;

import android.content.Context;
import android.content.Intent;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

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
 *  DB 연동해서 album list 얻어오기.
 *  받은 정보를 이용해, 갤러리의 첫번째 그림을 출력하도록 변경
 *
 */

public class AlbumsActivity extends AppCompatActivity {

    /* Modify by 김민혁 on 2016-10-24 */
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("albumtest");   //DB에서 Albumtest 명칭 변경시, 변경 필요

    public GridView mGridView;
    public GridAdapter gridAdapter;

    public final String TAG = "AlbumActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        Intent it = getIntent();
        final List albumList = new ArrayList();
        final List thumbnailUrls = new ArrayList();

        mGridView = (GridView)findViewById(R.id.gridview);
        gridAdapter = new GridAdapter(getApplicationContext(), R.layout.albums_thumbnail, thumbnailUrls);
        mGridView.setAdapter(gridAdapter);  // 커스텀 아답타를 GridView 에 적용// GridView 항목의 레이아웃 row.xml

        //앨범 클릭시 동작
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(AlbumsActivity.this, i + "번째 그림 선택",
                        Toast.LENGTH_SHORT).show();

                //GalleryActivity로 연결(DB 연동X)
                Intent intent = new Intent(AlbumsActivity.this, GalleryActivity.class);
                startActivity(intent);
            }
        });

        //앨범 list 가져오기
        myRef.addValueEventListener(
            new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    albumList.clear();
                    thumbnailUrls.clear();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        //String idx = child.getKey().toString();
                        if(child != null) {
//                            Log.d(TAG, "album_addValueEventListener : "+child.toString());
                            String albumName = child.child("name").getValue().toString();
                            String albumImgUrl = child.child("filelist").child("1").child("url").getValue().toString();
                            albumList.add(albumName);
                            thumbnailUrls.add(albumImgUrl);
                        }
                    }
                    gridAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    // ...
                }
            });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
//                return true;

            case R.id.action_favorite:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
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
     */
    class GridAdapter extends BaseAdapter {
        //리스트 layout을 위한 변수 3개
        Context context;
        int layout;
        LayoutInflater layoutInflater;
        List arrayList;     //정보 받아올 list

        public GridAdapter(Context context, int layout, List arrayList){
            this.context = context;
            this.layout = layout;
            this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.arrayList = arrayList;
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int i) {
            return arrayList.get(i);
        }

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

            return view;
        }
    }
}
