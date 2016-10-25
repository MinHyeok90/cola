package com.example.android.cola;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Ref;
import java.util.Map;

/**
 * Created by Krivnon on 2016-09-25.
 */
public class SignUpActivity extends AppCompatActivity implements TextView.OnEditorActionListener,View.OnClickListener {
    EditText SignId;
    EditText SignPw;
    EditText SignCpw;
    TextView mTvStatus;
    TextView mTvDetail;
    Button SignupBtn;

    private static final String TAG = "EmailPassword";

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        init();

    }
    void init()
    {
        //키보드 위로 edittext올리기
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        SignId = (EditText)findViewById(R.id.su_uid);
        SignPw = (EditText)findViewById(R.id.su_upw);
        SignCpw =  (EditText)findViewById(R.id.su_cupw);
        SignupBtn = (Button)findViewById(R.id.su_sign);

        mTvStatus = (TextView)findViewById(R.id.tvStatus);
        mTvDetail = (TextView)findViewById(R.id.tvDetail);

        SignupBtn.setOnClickListener(SignUpActivity.this);

        SignId.setOnEditorActionListener(this);
        SignPw.setOnEditorActionListener(this);
        SignCpw.setOnEditorActionListener(this);
//        // Read from the database
//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                String value = dataSnapshot.getValue(String.class);//return 값이 뭐든지 가능
//                Log.d(TAG, "Value is: " + value);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException());
//            }
//        });

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    User myUser = new User(user.getUid(), user.getEmail(), user.getDisplayName(), user.getPhotoUrl().toString());
                    myRef.child(user.getUid()).setValue(myUser);

                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }

                // [START_EXCLUDE]
                updateUI(user);
                // [END_EXCLUDE]
            }
        };
        // [END auth_state_listener]
    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    // [END on_stop_remove_listener]

    //버튼 클릭
    @Override
    public void onClick(View view) {
        String su_uid = SignId.getText().toString();
        String su_upw = SignPw.getText().toString();
        String su_cupw = SignPw.getText().toString();
        if (su_uid != null && su_upw !=null&& su_cupw !=null) {
            if(su_upw.equals(su_cupw))
                createAccount(su_uid, su_upw);
            else
                Toast.makeText(this,"비밀번호가 일치하지 않습니다.",Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this,"입력을 확인해주세요.",Toast.LENGTH_LONG).show();
        }

        //키보드 내리기
        view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        //showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(SignUpActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            mTvStatus.setText(R.string.auth_failed);
                        }
                        //hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }
    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        //showProgressDialog();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                       // hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }
    private boolean validateForm() {
        boolean valid = true;

        String email = SignId.getText().toString();
        if (TextUtils.isEmpty(email)) {
            SignId.setError("Required.");
            valid = false;
        } else {
            SignId.setError(null);
        }

        String password = SignPw.getText().toString();
        if (TextUtils.isEmpty(password)) {
            SignPw.setError("Required.");
            valid = false;
        } else {
            SignPw.setError(null);
        }

        return valid;
    }
    private void updateUI(FirebaseUser user) {
        //hideProgressDialog();
        if (user != null) {
            mTvStatus.setText(getString(R.string.emailpassword_status_fmt, user.getEmail()));
            mTvDetail.setText(getString(R.string.firebase_status_fmt, user.getUid()));

//            findViewById(R.id.email_password_buttons).setVisibility(View.GONE);
//            findViewById(R.id.email_password_fields).setVisibility(View.GONE);
//            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
        } else {
            mTvStatus.setText(R.string.signed_out);
            mTvDetail.setText(null);

//            findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
//            findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
//            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if(textView.getId()==R.id.su_uid && i== EditorInfo.IME_ACTION_NEXT){ // 뷰의 id를 식별, 키보드의 완료 키 입력 검출
            View view = this.getCurrentFocus();
        }
        if(textView.getId()==R.id.su_upw && i== EditorInfo.IME_ACTION_NEXT){ // 뷰의 id를 식별, 키보드의 완료 키 입력 검출
            View view = this.getCurrentFocus();
        }
        if(textView.getId()==R.id.su_cupw && i== EditorInfo.IME_ACTION_SEND){
            View view = this.getCurrentFocus();
            onClick(view);
        }
        return false;
    }
}
