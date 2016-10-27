package com.example.android.cola;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Krivnon on 2016-09-05.
 * <p>
 * Modify by 김민혁 on 2016-10-24
 * 메소드 수정 : onActivityResult
 * 로그인 성공하면 album 액티비티로 이동.
 */
public class LoginActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private static final String TAG = "Login";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("users");
    // [START declare_auth]
    // [START initialize_auth]
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    // [END initialize_auth]
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]

    private Button btnsignup;
    private Button btnlogin;
    private SignInButton btningmail;
    private Button btnoutgmail;
    private EditText myid;
    private EditText mypasswd;
    private Context mcontext;

    GoogleApiClient mGoogleApiClient;

    public LoginActivity() {
    }

    public LoginActivity(Context context) {
        mcontext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        Intent ulog = new Intent(this.getIntent());

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleApiClient = new GoogleApiClient.Builder(LoginActivity.this)
                .enableAutoManage(LoginActivity.this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    User myUser = new User(user.getUid(), user.getEmail(), user.getDisplayName(), user.getPhotoUrl().toString());
                    myRef.child(user.getUid()).setValue(myUser);

                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    Intent intent = new Intent(LoginActivity.this, AlbumsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // [START_EXCLUDE]
                updateUI(user);
                // [END_EXCLUDE]

            }
        };

        init();

    }

    void init() {
        btnlogin = (Button) findViewById(R.id.loginbtn);
        btnsignup = (Button) findViewById(R.id.signUpid);
        btningmail = (SignInButton) findViewById(R.id.sign_in_gmail);
        btnoutgmail = (Button) findViewById(R.id.sign_out_gmail);

        myid = (EditText) findViewById(R.id.editText);
        mypasswd = (EditText) findViewById(R.id.editText2);

        btnlogin.setOnClickListener(this);
        btnsignup.setOnClickListener(this);
        btningmail.setOnClickListener(this);
        btnoutgmail.setOnClickListener(this);
    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        // [START auth_state_listener]
        mAuth.addAuthStateListener(LoginActivity.this.mAuthListener);
        // [END auth_state_listener]

        super.onStart();
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

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            //mStatusTextView.setText(getString(R.string.google_status_fmt, user.getEmail()));
            //mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.sign_in_gmail).setVisibility(View.GONE);
            findViewById(R.id.sign_out_gmail).setVisibility(View.VISIBLE);
        } else {
            //mStatusTextView.setText(R.string.signed_out);
            //mDetailTextView.setText(null);

            findViewById(R.id.sign_in_gmail).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_gmail).setVisibility(View.GONE);
        }
    }

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            //handleSignInResult(result);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

                //Intent listintent = new Intent(this,FriendListActivity.class);
                //startActivity(listintent);
                ////로그인 성공시, 앨범집으로 이동합니다.
                Intent intent = new Intent(this, AlbumsActivity.class);
                startActivity(intent);
                finish();
            } else {
                Log.d(TAG, "failed: " + result.toString() + ", " + result.getStatus());
                // Google Sign In failed, update UI appropriately
                updateUI(null);
            }
        }
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    // [END auth_with_google]

//    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
//        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
//
//
//        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
//
//                        // If sign in fails, display a message to the user. If sign in succeeds
//                        // the auth state listener will be notified and logic to handle the
//                        // signed in user can be handled in the listener.
//                        if (!task.isSuccessful()) {
//                            Log.w(TAG, "signInWithCredential", task.getException());
//                            Toast.makeText(Login.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//    }


    // [START signin]
    //구글 아이디 로그인
    private void signIn() {
        showProgressDialog();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]

    //이메일 로그인시 아이디 비밀번호 valid 여부
    private boolean validateForm() {
        boolean valid = true;

        String email = myid.getText().toString();
        if (TextUtils.isEmpty(email)) {
            myid.setError("Required.");
            valid = false;
        } else {
            myid.setError(null);
        }

        String password = mypasswd.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mypasswd.setError("Required.");
            valid = false;
        } else {
            mypasswd.setError(null);
        }

        return valid;
    }

    //이메일 로그인
    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

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
                            Toast.makeText(LoginActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            ////로그인 성공시, 앨범집으로 이동합니다.
                            Intent intent = new Intent(LoginActivity.this, AlbumsActivity.class);
                            startActivity(intent);

                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {

                        }
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
        mypasswd.setText("");
        myid.setText("");
    }

    //로그아웃
    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(null);
                    }
                });
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {

                        updateUI(null);
                    }
                });
    }


//    private void handleSignInResult(GoogleSignInResult result){
//        Log.d(TAG,"handleSignInResult : "+result.isSuccess());
//        if(result.isSuccess()){
//            GoogleSignInAccount acct = result.getSignInAccount();
//            mStatusTextView.setText("Hello, "+acct.getDisplayName());
//        }else{
//            mStatusTextView.setText("handleSignInResult 로그인에 실패했습니다.");
//        }
//    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loginbtn:
                String id = myid.getText().toString();
                String pwd = mypasswd.getText().toString();
                FirebaseUser user = mAuth.getCurrentUser();

                if (user != null) {
                    //이미 로그인 되어져 있습니다.
                    Toast.makeText(LoginActivity.this, "이미 로그인 되어져 있습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                signIn(id, pwd);

                break;
            case R.id.signUpid:
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
                break;
            case R.id.sign_in_gmail:
                signIn();
                break;
            case R.id.sign_out_gmail:
                signOut();
                break;
        }
    }


}
