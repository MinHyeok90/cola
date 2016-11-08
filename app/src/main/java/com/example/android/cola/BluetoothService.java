package com.example.android.cola;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
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
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class BluetoothService extends Service {
    public final String TAG = "BluetoothService";

    private myServiceHandler mHandler;

    BluetoothAdapter mBluetoothAdapter ;
    BluetoothLeAdvertiser mBluetoothLeAdvertiser ;
    public final static int REQUEST_ENABLE_BT= 3;

    // Stops scanning after 30 seconds.
    private static final long SCAN_PERIOD = 30000;
    private static final String UID = "CDB7950D-73F1-4D4D-8E47-C090502DBD63";

    private BluetoothService mBluetoothService;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;
    private AdvertiseCallback mAdvertisingCallback;
    private ScanSettings settings;
    private List<ScanFilter> filters = new ArrayList();
    private List<FriendsDeviceLog> friendsDeviceList = new ArrayList();

    private DatabaseOpenHelper mDatabaseOpenHelper;

    private boolean isScanning = false;
    private boolean isAdvertising = false;

    private boolean isBluetoothInitiallyEnabled = false;

    long now;
    String strNow;
    //private ServiceThread mServiceThread;

    private TextView mText;
    public FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply,
                                     int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported", Toast.LENGTH_SHORT).show();
            return;
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

        now = System.currentTimeMillis();
        Date date = new Date(now);
        // 각자 사용할 포맷을 정하고 문자열로 만든다.
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        strNow = sdfNow.format(date);

        // DB Create and Open
        mDatabaseOpenHelper = new DatabaseOpenHelper(this);

        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                Log.e( "BLE", "onScanResult()");
                if( result == null || result.getDevice() == null) return;

                //result.getScanRecord().getServiceData().values();
                byte[] idBytes = result.getScanRecord().getServiceData(new ParcelUuid(UUID.fromString( "0000950d-0000-1000-8000-00805f9b34fb" ) ));
                String email = new String(idBytes, Charset.forName("UTF-8") ) + "@google.com";

                boolean isEqual = false;

                for(FriendsDeviceLog fdLog: friendsDeviceList){
                    if(fdLog.getUserID() == email){
                        isEqual =true;
                        break;
                    }
                }
                if(!isEqual) {
                    Log.e( "BLE", "not contain");
                    /*StringBuilder builder = new StringBuilder(email);
                    builder.append("\n");//.append(id);
                    */
                    Log.e( "BLE", "ScanResult(): "+ email);
                    //mText.setText(mText.getText() + builder.toString());
                    FriendsDeviceLog friendsDeviceLog = new FriendsDeviceLog(email, strNow);
                    friendsDeviceList.add(friendsDeviceLog);
                    mDatabaseOpenHelper.insert(friendsDeviceLog);
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                Log.e( "BLE", "onBatchScanResults()");
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e( "BLE", "Discovery onScanFailed: " + errorCode );
                super.onScanFailed(errorCode);
            }
        };
        mAdvertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                //mBluetoothLeAdvertiser.stopAdvertising(mAdvertisingCallback);
            }
            @Override
            public void onStartFailure(int errorCode) {
                Log.e( "BLE", "Advertising onStartFailure: " + errorCode );
                super.onStartFailure(errorCode);
            }
        };
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        //return mBinder;
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(),"서비스를 종료합니다.", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onDestroy()");
        if(isScanning){
            mBluetoothLeScanner.stopScan(mScanCallback);
            Log.i( "BLE", "stopScan()");
            isScanning = false;
        }
        if(isAdvertising) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertisingCallback);
            Log.i( "BLE", "stopAdvertising()");
            isAdvertising = false;
        }
        if(!isBluetoothInitiallyEnabled){
            Toast.makeText(getApplicationContext(),"블루투스를 다시 끕니다.", Toast.LENGTH_SHORT).show();
            mBluetoothAdapter.disable();
        }
        //mServiceThread.stopForever();
        //mServiceThread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        mHandler = new myServiceHandler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSelf();
            }
        }, SCAN_PERIOD);

        Toast.makeText(getApplicationContext(),"스레드를 시작합니다", Toast.LENGTH_SHORT).show();

        if (mBluetoothAdapter == null){
            Toast.makeText(getApplicationContext(),"블루투스를 지원 하지 않는 기기입니다", Toast.LENGTH_SHORT).show();
            Log.e( "BLE", "bluetoothLE Not Supported");
            stopSelf();
        } else if(!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(),"블루투스를 켭니다", Toast.LENGTH_SHORT).show();
            Log.e( "BLE", "bluetooth.enable()");
            while(!mBluetoothAdapter.isEnabled()){
                try{
                    mBluetoothAdapter.enable();
                }catch (Exception e){}
            }
            Log.e( "BLE", "bluetooth enabled");
            isBluetoothInitiallyEnabled = false;
        }
        else {
            isBluetoothInitiallyEnabled = true;
        }
        if(mBluetoothAdapter.isEnabled()){
            if (Build.VERSION.SDK_INT >= 21) {
                Log.e( "BLE", "Startscan()");
                Toast.makeText(getApplicationContext(),"Start Scan", Toast.LENGTH_SHORT).show();
                ScanFilter filter = new ScanFilter.Builder()
                        .setServiceUuid( new ParcelUuid(UUID.fromString( UID ) ) )
                        .build();
                filters.add( filter );

                ScanSettings settings = new ScanSettings.Builder()
                        .setScanMode( ScanSettings.SCAN_MODE_LOW_LATENCY )
                        .build();
                mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
                isScanning = true;
            }
        }
        if (!mBluetoothAdapter.isMultipleAdvertisementSupported() ) {
            Toast.makeText(getApplicationContext(),"This device is not support BLE Advertisement", Toast.LENGTH_SHORT).show();
        }
        else {
            if (Build.VERSION.SDK_INT >= 21) {
                Log.e( "BLE", "StartAdvertising()");
                Toast.makeText(getApplicationContext(),"Start Advertising", Toast.LENGTH_SHORT).show();
                AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                        .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                        .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                        .setConnectable( false )
                        .build();
                ParcelUuid pUuid = new ParcelUuid( UUID.fromString(UID) );

                byte[] bytes;
                String userUid = mUser.getEmail().split("@")[0];
                bytes = userUid.getBytes(Charset.forName( "UTF-8" ));

                AdvertiseData advertiseData = new AdvertiseData.Builder()
                        //.setIncludeDeviceName( true )
                        .addServiceUuid( pUuid )
                        .addServiceData( pUuid, bytes )
                        .build();

                mBluetoothLeAdvertiser.startAdvertising( advertiseSettings, advertiseData, mAdvertisingCallback );
                isAdvertising = true;
            }
        }
        return super.onStartCommand(intent, flags, startId);

    }
    class myServiceHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            Intent intent = new Intent(BluetoothService.this, BluetoothTestActivity.class);
            Log.d(TAG, "Handler.handlemessage()");
            //토스트 띄우기
            Toast.makeText(BluetoothService.this, "메시지 : " + msg.what, Toast.LENGTH_LONG).show();
        }
    };
}
