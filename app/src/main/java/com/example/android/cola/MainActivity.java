package com.example.android.cola;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Button btn1;
    Button btn2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1 = (Button)findViewById(R.id.hi);
        btn2 = (Button)findViewById(R.id.bye);
    }
    void onClick(View v){

        switch (v.getId()) {
            case R.id.hi:
                Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this,Albums.class);
                startActivity(intent);
                break;
            case R.id.bye:
                Toast.makeText(this, "goodbye", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
