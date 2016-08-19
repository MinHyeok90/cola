package com.example.android.cola;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button)findViewById(R.id.hi);
    }
    void onClick(View v){
        Toast.makeText(this,"hi",Toast.LENGTH_SHORT).show();//메시지 처리하기

    }

}
