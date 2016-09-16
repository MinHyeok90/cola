package com.example.android.cola;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class Albums extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        Intent it= getIntent();
        GridView grid = (GridView)findViewById(R.id.gridview);
        ImageAdapter Adapter = new ImageAdapter(this);
        grid.setAdapter(Adapter);

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(Albums.this, i + "번째 그림 선택",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}

class ImageAdapter extends BaseAdapter {
    private Context mContext;

    int[] picture = {
            R.drawable.ab,
            R.drawable.ab,
            R.drawable.ic_action_name,
            R.drawable.ab,
            R.drawable.ic_action_name
    };

    public ImageAdapter(Context c){
        mContext = c;
    }

    public int getCount(){
        return 12;
    }

    public Object getItem(int position){
        return picture[position % 5];
    }

    public long getItemId(int position){
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        ImageView imageView;
        if(convertView == null){
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(600,600));
            imageView.setAdjustViewBounds(false);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        }else{
            imageView = (ImageView)convertView;
        }

        imageView.setImageResource(picture[position % 5]);
        return imageView;
    }
}