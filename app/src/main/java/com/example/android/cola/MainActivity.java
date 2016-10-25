package com.example.android.cola;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private Button mLogin;
    private Button mAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLogin = (Button) findViewById(R.id.btLogin);
        mLogin.setOnClickListener(this);
        mAlbum = (Button) findViewById(R.id.btAlbum);
        mAlbum.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btAlbum:
                Toast.makeText(this, "hi", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, AlbumsActivity.class);
                startActivity(intent);
                break;

            case R.id.btLogin:
                Toast.makeText(this, "goodbye", Toast.LENGTH_SHORT).show();
                Intent logintent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(logintent);
                break;

            default:
                break;
        }
    }

}
