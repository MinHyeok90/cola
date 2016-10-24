package com.example.android.cola;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
// 사진 클릭 시 상세 화면 보여주는 액티비팅 아직 별거 없음 ㅎ
public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");

        ImageView imageView = (ImageView)findViewById(R.id.detailImageView);

        Glide.with(getApplicationContext())
                .load(url)
                .error(R.drawable.ic_action_name)
                .into(imageView);

    }
}
