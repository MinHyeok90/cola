package com.example.android.cola;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Krivnon on 2016-09-25.
 */
public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    EditText SignId;
    EditText SignPw;
    EditText SignCpw;
    Button SignupBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        init();

    }

    void init()
    {
        SignId = (EditText)findViewById(R.id.su_uid);
        SignPw = (EditText)findViewById(R.id.su_upw);
        SignCpw =  (EditText)findViewById(R.id.su_cupw);
        SignupBtn = (Button)findViewById(R.id.su_sign);

        SignupBtn.setOnClickListener(SignUpActivity.this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.su_sign:

                break;
        }
    }
}
