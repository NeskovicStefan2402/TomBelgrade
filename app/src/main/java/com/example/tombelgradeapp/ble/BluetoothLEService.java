package com.example.tombelgradeapp.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

public class BluetoothLEService extends Service {

    static final String TAG = "BluetoothLEService";

    public final static String ACTION_GATT_CONNECTED = "mikroe.com.blepclick.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "mikroe.com.blepclick.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "mikroe.com.blepclick.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_READ = "mikroe.com.blepclick.ACTION_DATA_READ";
    public final static String ACTION_DATA_NOTIFY = "mikroe.com.blepclick.common.ACTION_DATA_NOTIFY";
    public final static String ACTION_DATA_WRITE = "mikroe.com.blepclick.common.ACTION_DATA_WRITE";
    public final static String EXTRA_DATA = "mikroe.com.blepclick.EXTRA_DATA";
    public final static String EXTRA_UUID = "mikroe.com.blepclick.EXTRA_UUID";
    public final static String EXTRA_STATUS = "mikroe.com.blepclick.EXTRA_STATUS";
    public final static String EXTRA_ADDRESS = "mikroe.com.blepclick.EXTRA_ADDRESS";

    // BLE
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothGatt mBluetoothGatt = null;
    private static BluetoothLEService mThis = null;
    private String mBluetoothDeviceAddress;

    /**
     * GATT client callbacks
     */
    private BluetoothGattCallback mGattCallbacks = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            if (mBluetoothGatt == null) {
                Log.e(TAG, "mBluetoothGatt not created!");
                return;
            }

            BluetoothDevice device = gatt.getDevice();
            String address = device.getAddress();
            Log.d(TAG, "onConnectionStateChange (" + address + ") " + newState
                    + " status: " + status);

            try {
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        broadcastUpdate(ACTION_GATT_CONNECTED, address, status);
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        broadcastUpdate(ACTION_GATT_DISCONNECTED, address, status);
                        break;
                    default:
                        Log.e(TAG, "New state not processed: " + newState);
                        break;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothDevice device = gatt.getDevice();
            broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED,
                    device.getAddress(), status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_NOTIFY, characteristic,
                    BluetoothGatt.GATT_SUCCESS);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            broadcastUpdate(ACTION_DATA_READ, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            broadcastUpdate(ACTION_DATA_WRITE, characteristic, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor descriptor, int status) {
            Log.i(TAG, "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {
            Log.i(TAG, "onDescriptorWrite");
        }
    };

    private void broadcastUpdate(final String action, final String address,
                                 final int status) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_ADDRESS, address);
        intent.putExtra(EXTRA_STATUS, status);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic, final int status) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
        intent.putExtra(EXTRA_DATA, characteristic.getValue());
        intent.putExtra(EXTRA_STATUS, status);
        sendBroadcast(intent);
    }

    private boolean checkGatt() {
        if (mBtAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        if (mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return false;
        }
        return true;

    }

    /**
     * Manage the BLE service
     */
    public class LocalBinder extends Binder {
        public BluetoothLEService getService() {
            return BluetoothLEService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that
        // BluetoothGatt.close() is called
        // such that resources are cleaned up properly. In this particular
        // example,
        // close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder binder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {

        Log.d(TAG, "initialize");
        // For API level 18 and above, get a reference to BluetoothAdapter
        // through
        // BluetoothManager.
        mThis = this;
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBtAdapter = mBluetoothManager.getAdapter();
        if (mBtAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    //
    // GATT API
    //
    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic
     *            The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (!checkGatt())
            return;
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, byte b) {
        if (!checkGatt())
            return false;

        byte[] val = new byte[1];
        val[0] = b;
        characteristic.setValue(val);
        return mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, boolean b) {
        if (!checkGatt())
            return false;

        byte[] val = new byte[1];

        val[0] = (byte) (b ? 1 : 0);
        characteristic.setValue(val);
        return mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (!checkGatt())
            return false;
        return mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * Retrieves the number of GATT services on the connected device. This
     * should be invoked only after {@code BluetoothGatt#discoverServices()}
     * completes successfully.
     *
     * @return A {@code integer} number of supported services.
     */
    public int getNumServices() {
        if (mBluetoothGatt == null)
            return 0;

        return mBluetoothGatt.getServices().size();
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This
     * should be invoked only after {@code BluetoothGatt#discoverServices()}
     * completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null)
            return null;

        return mBluetoothGatt.getServices();
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic
     *            Characteristic to act on.
     * @param //enabled
     *            If true, enable notification. False otherwise.
     */
    public boolean setCharacteristicNotification(
            BluetoothGattCharacteristic characteristic, boolean enable) {
        if (!checkGatt())
            return false;

        if (!mBluetoothGatt.setCharacteristicNotification(characteristic,
                enable)) {
            Log.w(TAG, "setCharacteristicNotification failed");
            return false;
        }

        BluetoothGattDescriptor clientConfig = characteristic
                .getDescriptor(GattInfo.CLIENT_CHARACTERISTIC_CONFIG);
        if (clientConfig == null)
            return false;

        if (enable) {
            Log.i(TAG, "enable notification");
            clientConfig
                    .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            Log.i(TAG, "disable notification");
            clientConfig
                    .setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        return mBluetoothGatt.writeDescriptor(clientConfig);
    }

    public boolean isNotificationEnabled(
            BluetoothGattCharacteristic characteristic) {
        if (!checkGatt())
            return false;

        BluetoothGattDescriptor clientConfig = characteristic
                .getDescriptor(GattInfo.CLIENT_CHARACTERISTIC_CONFIG);
        if (clientConfig == null)
            return false;

        return clientConfig.getValue() == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address
     *            The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The
     *         connection result is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBtAdapter == null || address == null) {
            Log.w(TAG,
                    "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        final BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
        int connectionState = mBluetoothManager.getConnectionState(device,
                BluetoothProfile.GATT);

        if (connectionState == BluetoothProfile.STATE_DISCONNECTED) {

            // Previously connected device. Try to reconnect.
            if (mBluetoothDeviceAddress != null
                    && address.equals(mBluetoothDeviceAddress)
                    && mBluetoothGatt != null) {
                Log.d(TAG, "Re-use GATT connection");
                if (mBluetoothGatt.connect()) {
                    return true;
                } else {
                    Log.w(TAG, "GATT re-connect failed.");
                    return false;
                }
            }

            if (device == null) {
                Log.w(TAG, "Device not found.  Unable to connect.");
                return false;
            }
            // We want to directly connect to the device, so we are setting the
            // autoConnect parameter to false.
            Log.d(TAG, "Create a new GATT connection.");
            mBluetoothGatt = device.connectGatt(this, false, mGattCallbacks);
            mBluetoothDeviceAddress = address;
        } else {
            Log.w(TAG, "Attempt to connect in state: " + connectionState);
            return false;
        }
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect(String address) {
        if (mBtAdapter == null) {
            Log.w(TAG, "disconnect: BluetoothAdapter not initialized");
            return;
        }
        final BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
        int connectionState = mBluetoothManager.getConnectionState(device,
                BluetoothProfile.GATT);

        if (mBluetoothGatt != null) {
            Log.i(TAG, "disconnect");
            if (connectionState != BluetoothProfile.STATE_DISCONNECTED) {
                mBluetoothGatt.disconnect();
            } else {
                Log.w(TAG, "Attempt to disconnect in state: " + connectionState);
            }
        }
    }

    /**
     * After using a given BLE device, the app must call this method to ensure
     * resources are released properly.
     */
    public void close() {
        if (mBluetoothGatt != null) {
            Log.i(TAG, "close");
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    public int numConnectedDevices() {
        int n = 0;

        if (mBluetoothGatt != null) {
            List<BluetoothDevice> devList;
            devList = mBluetoothManager
                    .getConnectedDevices(BluetoothProfile.GATT);
            n = devList.size();
        }
        return n;
    }

    //
    // Utility functions
    //
    public static BluetoothGatt getBtGatt() {
        return mThis.mBluetoothGatt;
    }

    public static BluetoothManager getBtManager() {
        return mThis.mBluetoothManager;
    }

    public static BluetoothLEService getInstance() {
        return mThis;
    }
}
