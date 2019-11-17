package com.example.tombelgradeapp.rest;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.tombelgradeapp.R;
import com.example.tombelgradeapp.ble.BluetoothLEManager;
import com.example.tombelgradeapp.ble.BluetoothLEService;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BuggyControl  extends Activity {

    private final static String TAG = BuggyControl.class.getSimpleName();

    public static String EXTRAS_DEVICE_NAME = "DEVICE NAME";
    public static String EXTRAS_DEVICE_ADDRESS = "DEVICE ADDRESS";

    public static BluetoothDevice Device = null;

    private BluetoothLEService mBluetoothLeService;
    private BluetoothGattService mBluetoothGattService;
    private BluetoothLEManager mBtManager;

    private boolean mConnected = false;

    private String mDeviceName;
    private String mDeviceAddress;

    BluetoothGattCharacteristic blepTx = null;
    BluetoothGattCharacteristic blepRx = null;

    Timer BuggyResponseTimer = new Timer();
    final Handler handler = new Handler();

    private TimerTask mTimerTask;



    private static int bSpeed = 0;
    private static int bSpeedOld = bSpeed;

    private TextView valBattLevel;
    private ProgressBar valPower;

    //COMMANDS
    static final byte CMD_DRIVE = 0x01;
    static final byte CMD_BREAK = 0x02;
    static final byte CMD_RESPOND = 0x03;
    static final byte CMD_SPIN = 0x04;

    private byte buggyCMD;
    private byte txBuffer[] = new byte[]{0x06,0,0,0,0,0};
    private byte txTest[] = new byte[]{'5'};
    private int BatteryLevel = 0;

    private Switch mSpin;
    private ToggleButton mHeadLights;
    private ToggleButton mMainLights;
    private ToggleButton mTurnLeft;
    private ToggleButton mTurnRight;

    static final byte POWER_MIN = 20;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_buggy_control);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        //getActionBar().setDisplayHomeAsUpEnabled(true);
        mBtManager = BluetoothLEManager.getInstance();
        mBtManager.startBluetoothLeService(this, mServiceConnection);


        /*
        mSpin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked) {
                    txBuffer[1] = CMD_BREAK;
                }
            }
        });

         */

        //Init values
        buggyCMD = CMD_BREAK;
    }



    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            //if(EXTRAS_DEVICE_ADDRESS != null) {
            //registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
            //}
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stopTimerTask();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimerTask();
        mConnected = false;
        mBluetoothLeService.disconnect(mDeviceAddress);
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.buggy_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }



    private void showBatteryLevel() {
        double result;
        //result = 1.8 * BatteryLevel / 4096 * 3; //mikromedia
        result = 3.3 * BatteryLevel / 4096 * 3;  //clicker2
        valBattLevel.setText(String.format("%.2f",result) + " V");
    }

    private void displayReceivedData() {
        showBatteryLevel();
    }



    private void updateConnectionState(final int resourceId) {

        if(resourceId == R.string.connected) {
            mConnected = true;
            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doTimerTask();
                }
            });*/
        }else {
            mConnected = false;
            stopTimerTask();
        }

    }



    private void clearUI() {
        //ToDo
    }


    private void updateControls() {


    }


    private void doTimerTask(){
     /*   mTimerTask = new TimerTask() {
            public void run() {
                updateControls();
            }
        };
        // public void schedule (TimerTask task, long delay, long period)
        BuggyResponseTimer.schedule(mTimerTask, 100, 100);*/

    }

    public void stopTimerTask(){
        if(mTimerTask!=null){
            handler.post(new Runnable() {
                public void run() {
                    clearUI();
                }
            });
            mTimerTask.cancel();
            mTimerTask=null;
        }
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                          BLE SERVICE                                       //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLEService.LocalBinder) service).getService();
            System.out.println("DEBUG NAS onServiceConnected " + mBluetoothLeService.toString());
            if (!mBluetoothLeService.initialize()) {
                finish(); //Close Application
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
            System.out.println("DEBUG NAS onServiceConnected proslo connect metodu");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            System.out.println("DEBUG NAS onServiceDisconnected ");
            mBluetoothLeService = null;
        }
    };


    public void sendFive(View view) {
        System.out.println("Send Five");
        System.out.println(mConnected);
        if((blepTx != null) && mConnected) {
            System.out.println("Prosao if");
            blepTx.setValue(txTest);
            mBluetoothLeService.writeCharacteristic(blepTx);
        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("Debug onReceive");
            final String action = intent.getAction();
            System.out.println(action);
            int status = intent.getIntExtra(BluetoothLEService.EXTRA_STATUS, BluetoothGatt.GATT_SUCCESS);

            if (BluetoothLEService.ACTION_GATT_CONNECTED.equals(action)) {
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
                if(BluetoothLEService.getBtGatt() != null)
                {
                    BluetoothLEService.getBtGatt().discoverServices();
                }
            } else if (BluetoothLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

                GetBlepGattService(mBluetoothLeService.getSupportedGattServices());

                if (mBluetoothGattService != null) {


                    mBluetoothLeService.setCharacteristicNotification(blepRx,true);

                }

            }
            else if (BluetoothLEService.ACTION_DATA_NOTIFY.equals(action)) {
                // Notification
                byte  [] value = intent.getByteArrayExtra(BluetoothLEService.EXTRA_DATA);
                String uuidStr = intent.getStringExtra(BluetoothLEService.EXTRA_UUID);
                onCharacteristicChanged(uuidStr, value);
            } else if (BluetoothLEService.ACTION_DATA_WRITE.equals(action)) {
                // Data written
                String uuidStr = intent.getStringExtra(BluetoothLEService.EXTRA_UUID);
                onCharacteristicWrite(uuidStr,status);
            } else if (BluetoothLEService.ACTION_DATA_READ.equals(action)) {
                // Data read
                String uuidStr = intent.getStringExtra(BluetoothLEService.EXTRA_UUID);
                byte  [] value = intent.getByteArrayExtra(BluetoothLEService.EXTRA_DATA);
                onCharacteristicsRead(uuidStr,value,status);
            }
        }
    };


    //List services and find our pref. service
    private void GetBlepGattService(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        mBluetoothGattService = null;
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            //Log.d(TAG, uuid);
            if(uuid.equals(BlepUtils.BLEP_SERVICE) ) {

                //Log.d(TAG, "Find BLEP SERVICE");
                mBluetoothGattService = gattService; //Set our prefered service
                // Loops through available Characteristics.
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                gattService.getUuid();
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                    uuid = gattCharacteristic.getUuid().toString();

                    System.out.println("UUID " + uuid);

                    if(uuid.equals(BlepUtils.nRF_TX)){

                        blepTx = gattCharacteristic;
                        //Log.d(TAG, "Find TX Charasteristics");
                    }

                    if(uuid.equals(BlepUtils.nRF_RX)){

                        blepRx = gattCharacteristic;
                        //Log.v(TAG, "Find RX Charasteristics");
                    }

                }
            }
        }

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                        EVENTS                                              //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLEService.ACTION_DATA_NOTIFY);
        intentFilter.addAction(BluetoothLEService.ACTION_DATA_READ);
        intentFilter.addAction(BluetoothLEService.ACTION_DATA_WRITE);
        return intentFilter;
    }

    private void onCharacteristicWrite(String uuidStr, int status) {

        //ToDo
        //User code
    }

    private void onCharacteristicChanged(String uuidStr, byte[] value) {


        if (value != null && value.length > 0) {
            //battery level
            BatteryLevel = BlepUtils.getIntValue(value,BlepUtils._UINT16,0);
            displayReceivedData();
        }

        //Log.d(TAG, "Caracteristics Changed");
    }

    private void onCharacteristicsRead(String uuidStr, byte[] value, int status) {

        //User code
        //Log.d(TAG, "Caracteristics Read");
    }
}
