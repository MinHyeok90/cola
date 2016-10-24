package com.example.android.cola;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
 *  앨범 선택하면 GalleryActivity로 연결.
 *
 */

public class Albums extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        Intent it = getIntent();
        GridView grid = (GridView) findViewById(R.id.gridview);
        ImageAdapter_main Adapter = new ImageAdapter_main(this);
        grid.setAdapter(Adapter);

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(Albums.this, i + "번째 그림 선택",
                        Toast.LENGTH_SHORT).show();

                //앨범선택시, 해당 앨범으로 이동합니다.
                Intent intent = new Intent(Albums.this,GalleryActivity.class);
                startActivity(intent);
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
}

class ImageAdapter_main extends BaseAdapter {
    private Context mContext;

    //Test images
    int[] picture = {
            R.drawable.ab,
            R.drawable.ab,
            R.drawable.ic_action_name,
            R.drawable.ab,
            R.drawable.ic_action_name
    };

    public ImageAdapter_main(Context c) {
        mContext = c;
    }

    public int getCount() {
        return 12;
    }

    public Object getItem(int position) {
        return picture[position % 5];
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(600, 600));
            imageView.setAdjustViewBounds(false);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(picture[position % 5]);
        return imageView;
    }
}