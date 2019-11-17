package com.example.tombelgradeapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.speech.RecognizerIntent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.example.tombelgradeapp.ble.BluetoothLEManager;
import com.example.tombelgradeapp.ble.BluetoothLEService;
import com.example.tombelgradeapp.rest.BlepUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    Button btn;
    public static int brojac=0;
    public static MediaPlayer player;
    private int lastClickTime= (int) System.currentTimeMillis();
    private int init = 0;

    private BluetoothLEService mBluetoothLeService;

    private BluetoothLeScanner bluetoothLeScanner;
    List<BluetoothDevice> lista=new ArrayList<>();
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 30000;
    private String bluetoothNameDevice;
    private boolean mConnected = false;
    public static DataController myDb;

    private boolean firstTime = true;


    private BluetoothGattService mBluetoothGattService;

    BluetoothGattCharacteristic blepTx = null;
    BluetoothGattCharacteristic blepRx = null;

    private byte txTest[] = new byte[]{'1'};
    private char[] chars = {'1'};
    private String bluetoothAddressDevice;

    private BluetoothLEManager mBtManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        lista.add(null);
        btn=findViewById(R.id.Pretrazi);
        Controller.pustiPocetniSignal(this);
        myDb = new DataController(this);
        myDb.initDatabase();
        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                prebaciIntent();
                return true;
            }
        });
        //////////////////////////////////////////
        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

    }


    private void scanLeDevice(final boolean enable) {
        String s = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";

        final ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build();
        final List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(s)).build());

        if (enable) {
            getmLeScanCallback();
            bluetoothLeScanner.startScan(filters, settings, mLeScanCallback);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothLeScanner.stopScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
        }
        else {
            mScanning = false;
            bluetoothLeScanner.stopScan(mLeScanCallback);
        }
    }


    public void pretraga(View view) {
        final MainActivity main=this;
        if (init == 0) {
            init++;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (init == 1) {
                        if (!Controller.player.isPlaying()) {
                            btn.setText(Controller.getTextButton(main));
                            if (firstTime) {
                                firstTime = false;
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        govor();
                                    }
                                }, 3000);
                            }
                            else {
                                if (mBluetoothLeService != null) {
                                    mBluetoothLeService.disconnect(bluetoothAddressDevice);
                                    mConnected = false;
                                    //unbindService(mServiceConnection);
                                }
                                firstTime = true;
                            }


                        }

                    }else {
                            Intent i=new Intent(main,ListOfThings.class);
                            startActivity(i);

                        }
                    init = 0;
                }

            }, 500);
        } else {
            init++;
        }




    }

    public void govor(){
        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());
        try{
           startActivityForResult(intent,1000);
        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {



        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case 1000:{
                if(resultCode==RESULT_OK && null!=data){
                    ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    LinkedList<String> lista=Controller.getLista();
                    Toast.makeText(this, ""+result.get(0), Toast.LENGTH_SHORT).show();
                    String name_ble=myDb.daLiPostoji(result.get(0));
                    if(!name_ble.isEmpty()){
                        //Posalji
                        this.bluetoothNameDevice = name_ble;
                        name_ble.getChars(6, 7, chars, 0);
                        scanLeDevice(true);
                           Controller.talkMetoda(this,"Your "+result.get(0)+" is located.");
                     }else{
                        Controller.talkMetoda(this,"Your "+result.get(0)+" is not located. Try again!");
                    }
                }
            }
            break;
        }

    }
    public void prebaciIntent(){
        Intent i=new Intent(this,DodajActivity.class);
        startActivity(i);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = MainActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            RecyclerView.ViewHolder viewHolder = null;
            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();


            return null;
        }
    }


    private void bluetoothConnect() {
        if (mBtManager == null) {
            mBtManager = BluetoothLEManager.getInstance();
            mBtManager.startBluetoothLeService(this, mServiceConnection);
        }
        else {
            mBluetoothLeService.connect(lista.get(0).getAddress());
        }

    }


    public void sendTrigger(byte id) {
        if((blepTx != null) && mConnected) {
            txTest[0] = id;
            blepTx.setValue(txTest);
            mBluetoothLeService.writeCharacteristic(blepTx);
        }
    }



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
            System.out.println(lista.get(0).getName());
            mBluetoothLeService.connect(lista.get(0).getAddress());
            System.out.println("DEBUG NAS onServiceConnected proslo connect metodu");
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            System.out.println("DEBUG NAS onServiceDisconnected ");
            mBluetoothLeService = null;
        }
    };




    private void getmLeScanCallback() {
        mLeScanCallback = new ScanCallback() {

            @Override
            public String toString() {
                return "nesto";
            }

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                System.out.println("DEBUG Uslo se u onScanResult");
                if (result != null) {
                    BluetoothDevice bluetoothDevice = result.getDevice();
                    System.out.println(bluetoothNameDevice);
                    System.out.println(bluetoothDevice.getName());
                    if (bluetoothDevice != null) {
                        if (bluetoothDevice.getName().toLowerCase().equals(bluetoothNameDevice.toLowerCase())) {
                            bluetoothAddressDevice = bluetoothDevice.getAddress();
                            lista.set(0, bluetoothDevice);
                            bluetoothLeScanner.stopScan(mLeScanCallback);
                            bluetoothConnect();
                        }
                    }
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                System.out.println("DEBUG Uslo se u onBatchScanResult");
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                System.out.println("DEBUG Uslo se u onScanFailed");
            }
        };

    }


    private ScanCallback mLeScanCallback = null;


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

    private void onCharacteristicWrite(String uuidStr, int status) {
    }

    private void onCharacteristicsRead(String uuidStr, byte[] array, int status) {
    }



    private void onCharacteristicChanged(String uuidStr, byte[] value) {

        //Log.d(TAG, "Caracteristics Changed");
    }





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

                        sendTrigger((byte)chars[0]);
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




}


