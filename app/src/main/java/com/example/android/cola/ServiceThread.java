package com.example.android.cola;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by KM on 2016. 11. 6..
 */
public class ServiceThread extends Thread{
    public final String TAG = "ServiceThread";
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private int count= 0;

    public ServiceThread(Handler handler){
        this.mScanning = true;
        this.mHandler = handler;
        Log.d(TAG, "ServiceThread 호출");

    }

    public void stopForever(){
        synchronized (this) {
            this.mScanning = false;
            Log.d(TAG, "StopForever()");
        }
    }

    public void run(){
        //반복적으로 수행할 작업을 한다.
        while(mScanning){
            mHandler.sendEmptyMessage(count);//쓰레드에 있는 핸들러에게 메세지를 보냄
            Log.d(TAG, "SendEmptyMessage()" + count++);
            try{
                this.sleep(5000); //10초씩 쉰다.
                Log.d(TAG, "Thread.sleep()");
            }catch (Exception e) {}
        }
    }


}