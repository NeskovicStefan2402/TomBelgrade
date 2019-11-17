package com.example.tombelgradeapp.ble;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;

public class BluetoothLEManager {

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private Handler mHandler = new Handler();
    public boolean mScanning;
    private static BluetoothLEManager mInstance;

    public BluetoothLEManager() {

    }

    public static BluetoothLEManager getInstance() {
        if (mInstance == null) {
            mInstance = new BluetoothLEManager();
        }
        return mInstance;
    }

    public BluetoothAdapter getAdapter(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context
                .getSystemService(Context.BLUETOOTH_SERVICE);
        return bluetoothManager.getAdapter();
    }

    public void scanLeDevice(final BluetoothAdapter bluetoothAdapter, final ScanCallback scanCallback, final boolean enable) {
        final BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothLeScanner.stopScan(scanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            bluetoothLeScanner.startScan(scanCallback);
        } else {
            mScanning = false;
            bluetoothLeScanner.stopScan(scanCallback);
        }
    }

    public void startBluetoothLeService(Context context, ServiceConnection serviceConnection)
    {
        Intent gattServiceIntent = new Intent(context, BluetoothLEService.class);
        context.bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


}
