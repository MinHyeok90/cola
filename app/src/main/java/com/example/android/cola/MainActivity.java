package com.example.android.cola;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    Button btn1;
    Button btn2;
    String emotion;
    private static final String TAG = "MainActivity";
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("msg");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1 = (Button)findViewById(R.id.hi);
        btn2 = (Button)findViewById(R.id.bye);
        init();
    }
    void init(){
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);//return 값이 뭐든지 가능

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }
    void onClick(View v){

        switch (v.getId()) {
            case R.id.hi:
                Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show();

                emotion = "hello";
                myRef.setValue(emotion);

                //Album 액티비티 test용 Intent
                Intent intent = new Intent(this,Albums.class);
                startActivity(intent);
                break;
            case R.id.bye:
                Toast.makeText(this, "goodbye", Toast.LENGTH_SHORT).show();
                emotion = "goodbye";
                myRef.setValue(emotion);
                Intent logintent = new Intent(this,Login.class);
                startActivity(logintent);
                break;
            case R.id.goodnight:
                //Album 액티비티 test용 Intent
                Intent galleryIntent = new Intent(this,GalleryActivity.class);
                startActivity(galleryIntent);
                break;
        }
    }

}
