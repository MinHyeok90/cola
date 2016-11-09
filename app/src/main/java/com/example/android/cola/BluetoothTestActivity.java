package com.example.android.cola;

import android.*;
import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BluetoothTestActivity extends AppCompatActivity {
    private Button btnStart, btnEnd, btnChk, btnDrop, btnFind;
    public final String TAG = "BluetoothActivity";

    private static final int PERMISSIONS_ACCESS_COARSE_LOCATION = 111;
    private static final int PERMISSIONS_ACCESS_FINE_LOCATION = 112;
    private static final String STRING_TRASH = "NOT_A_DATA_TRASH";
    private static final String UID = "CDB7950D-73F1-4D4D-8E47-C090502DBD63";

    private DatabaseOpenHelper mDatabaseOpenHelper;

    private TextView mText;
    public FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_test);

        /* 안드로이드 6.0 이후 내부 저장소 접근하려면 권한 승인 필요요 */
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(BluetoothTestActivity.this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(BluetoothTestActivity.this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(BluetoothTestActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_ACCESS_COARSE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        if (ContextCompat.checkSelfPermission(BluetoothTestActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(BluetoothTestActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(BluetoothTestActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        /*AlarmManager mAlarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        PendingIntent mAlarmSender = PendingIntent.getService(BluetoothTestActivity.this,
                0, new Intent(BluetoothTestActivity.this, BluetoothService.class), 0);

        //현재시간부터 시작하도록 시간을 가져옴
        long firstTime = SystemClock.elapsedRealtime();

        //현재시간부터, 60초 주기로 반복 알람이 발생하여,
        //매 알람시마다 서비스가 실행되도록 설정
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,firstTime, 60*1000, mAlarmSender);*/


        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        Log.i(TAG, "onCreate");

        btnStart = (Button)findViewById(R.id.bluetoothConnectButton);
        btnEnd = (Button)findViewById(R.id.bluetoothDisconnectButton);
        btnChk = (Button)findViewById(R.id.check_db);
        btnDrop = (Button)findViewById(R.id.drop_db);
        btnFind = (Button)findViewById(R.id.find_friends);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"서비스 시작되었습니다", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(BluetoothTestActivity.this,BluetoothService.class);
                startService(intent);
            }
        });

        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"서비스 종료", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(BluetoothTestActivity.this,BluetoothService.class);
                stopService(intent);
            }

        });
        btnChk.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mDatabaseOpenHelper = new DatabaseOpenHelper(getApplicationContext());
                mText = (TextView) findViewById(R.id.bluetoothTestTextView);

                List<FriendsDeviceLog> friendsDeviceLogs = mDatabaseOpenHelper.getAllFriendsDeviceLogs();
                String tvText = "";
                for(FriendsDeviceLog logs : friendsDeviceLogs){
                    //Date date = new Date(Long.parseLong(logs.getTimestamp()));

                    tvText += logs.getUserID() + " "+ logs.getTimestamp() +"\n";
                }
                mText.setText(tvText);
                //stopService(intent);
            }
        });
        btnDrop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mDatabaseOpenHelper = new DatabaseOpenHelper(getApplicationContext());
                mDatabaseOpenHelper.dropTable();
            }
        });
        btnFind.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(getApplicationContext(),"로그 분석 시작", Toast.LENGTH_SHORT).show();
                /*Intent intent = new Intent(BluetoothTestActivity.this,FindNearbyFriendsService.class);
                startService(intent);*/
                mDatabaseOpenHelper = new DatabaseOpenHelper(getApplicationContext());
                mText = (TextView) findViewById(R.id.bluetoothTestTextView);

                List<FriendsDeviceLog> friendsDeviceLogs = mDatabaseOpenHelper.getAllFriendsDeviceLogs();
                String tvText = "";
                String start="", end="";
                boolean flag = false;
                List<Group> groups = new ArrayList<Group>();

                if(!friendsDeviceLogs.isEmpty()){
                    List<String> emailList = new ArrayList<String>();

                    for(int i=0; i<friendsDeviceLogs.size(); i++){
                        FriendsDeviceLog fcur = friendsDeviceLogs.get(i);
                        // i==0일때 친구가 있으면
                        if(i==0){
                            if( !fcur.getUserID().matches(STRING_TRASH)) {
                                start = fcur.getTimestamp();
                                flag = true;

                                String curEmail = fcur.getUserID();
                                if(!emailList.contains(curEmail)) {
                                    emailList.add(curEmail);
                                }
                            }
                        }
                        else{ //i > 0
                            FriendsDeviceLog fprev = friendsDeviceLogs.get(i-1);
                            // 이전에 친구 없었고 지금 친구 있으면 -- 그롭 시작
                            if(fprev.getUserID().matches(STRING_TRASH) && !fcur.getUserID().matches(STRING_TRASH)){
                                // 시작 시간 : 지금
                                start = fcur.getTimestamp();
                                flag = true;
                                String curEmail = fcur.getUserID();
                                if(!emailList.contains(curEmail)) {
                                    emailList.add(curEmail);
                                }
                            }
                            // 이전에 친구 있었는데 지금 없으면 -- 그룹 끝
                            if(!fprev.getUserID().matches(STRING_TRASH) && fcur.getUserID().matches(STRING_TRASH)){
                                end = fprev.getTimestamp();
                                flag = false;

                                groups.add(new Group(start, end, emailList));
                                emailList = new ArrayList<String>();
                            }
                        }
                        if(flag) {
                            String curEmail = fcur.getUserID();
                            if(!emailList.contains(curEmail)) {
                                emailList.add(curEmail);
                            }
                            // 마지막 루프인데 flag가 참이면
                            if (i == friendsDeviceLogs.size() - 1) {
                                end = fcur.getTimestamp();
                                groups.add(new Group(start, end, emailList));
                            }

                        }
                    }
                }
                for(Group g : groups){
                    tvText += g.getStartTime() + " " + g.getEndTime() + "\n";
                    for(String s : g.getNameList()){
                        tvText+= s + " ";
                    }
                    tvText += "\n\n";
                }
                mText.setText(tvText);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class Group{
        private String startTime;
        private String endTime;
        private List<String> nameList;

        public Group(String startTime, String endTime, List<String> nameList) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.nameList = nameList;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public List<String> getNameList() {
            return nameList;
        }

        public void setNameList(List<String> nameList) {
            this.nameList = nameList;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }
    }
}
