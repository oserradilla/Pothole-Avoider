package com.oscarsc.potholeavoider.ble;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.oscarsc.potholeavoider.R;
import com.oscarsc.potholeavoider.incidences.*;
import com.oscarsc.potholeavoider.listeners.GpsListener;
import com.oscarsc.potholeavoider.services.Analyzer;
import com.oscarsc.potholeavoider.text_to_speech.MyTextToSpeech;

import org.apache.commons.lang3.StringUtils;

import java.util.Scanner;

/**
 * Created by oscar on 1/21/15.
 */
//TODO delete loops
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class Ble {
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 3000;
    private Activity activity;
    private boolean connected=false;
    private MyTextToSpeech tts;
    GpsListener gpsListener;
    public Ble(final Activity activity,MyTextToSpeech tts, GpsListener gpsListener) {
        this.activity=activity;
        this.gpsListener=gpsListener;
        if (!activity.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(activity, "Ble not supported", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        final BluetoothManager mBluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(activity, "Ble not supported", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        scanLeDevice();

        //showRoundProcessDialog(activity, R.layout.loading_process_dialog_anim);
        this.tts=tts;
    }

    public void showRoundProcessDialog(Context mContext, int layout) {
        DialogInterface.OnKeyListener keyListener = new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_HOME
                        || keyCode == KeyEvent.KEYCODE_SEARCH) {
                    return true;
                }
                return false;
            }
        };

        AlertDialog mDialog = new AlertDialog.Builder(mContext).create();
        mDialog.setOnKeyListener(keyListener);
        mDialog.show();

        mDialog.setContentView(layout);
    }

    private void scanLeDevice() {
        new Thread() {

            @Override
            public void run() {
                mBluetoothAdapter.startLeScan(mLeScanCallback);

                try {
                    Thread.sleep(SCAN_PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }.start();
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                             byte[] scanRecord) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (device != null) {
                        if(!connected) {
                            connectService(device);
                            makeGattUpdateIntentFilter();
                            activity.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
                            connected=true;
                        }
                        /*ParcelUuid uuids[]=device.getUuids();
                        for(ParcelUuid uuid: uuids)
                        if(uuid.getUuid().equals(new UUID("713d0000-503e-4c75-ba94-3148f18d941e")))
                            connectService(device);*/
                    }
                }
            }).start();
        }
    };

    private IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(RBLService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(RBLService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(RBLService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(RBLService.ACTION_DATA_AVAILABLE);

        return intentFilter;
    }


    //Connection part
    private RBLService mBluetoothLeService;
    private ServiceConnection mServiceConnection;

    private void connectService(final BluetoothDevice device) {
        mServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName,
                                           IBinder service) {
                mBluetoothLeService = ((RBLService.LocalBinder) service)
                        .getService();
                if (!mBluetoothLeService.initialize()) {
                    Log.e("Ble", "Unable to initialize Bluetooth");
                }
                // Automatically connects to the device upon successful start-up
                // initialization.
                mBluetoothLeService.connect(device.getAddress());
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mBluetoothLeService = null;
            }
        };

        Intent gattServiceIntent = new Intent(activity, RBLService.class);
        activity.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (RBLService.ACTION_GATT_DISCONNECTED.equals(action)) {
            } else if (RBLService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                getGattService(mBluetoothLeService.getSupportedGattService());
            } else if (RBLService.ACTION_DATA_AVAILABLE.equals(action)) {
                analyzeReceivedData(intent.getByteArrayExtra(RBLService.EXTRA_DATA));
            }
        }
    };
    private void getGattService(BluetoothGattService gattService) {
        if (gattService == null)
            return;

        BluetoothGattCharacteristic characteristic = gattService
                .getCharacteristic(RBLService.UUID_BLE_SHIELD_TX);
        System.out.print("Ble characteristics:\n" +
                "uuid: " + characteristic.getUuid() +
                "\nchar: " + characteristic);

        BluetoothGattCharacteristic characteristicRx = gattService
                .getCharacteristic(RBLService.UUID_BLE_SHIELD_RX);
        mBluetoothLeService.setCharacteristicNotification(characteristicRx,
                true);
        mBluetoothLeService.readCharacteristic(characteristicRx);
    }

    /*
    *Structure of received data:
    * i_p_5 | 1- i -> incidence | p -> type of incidence, in this case pothole (can also be c curve or s slope)
    * 5 -> magnitude of the incidence
     */
    private void analyzeReceivedData(byte[] byteArray) {
        if (byteArray != null) {
            String data = new String(byteArray);
            /* Create and launch the service that is going to be analyzing data from the Arduino via bluetooth */
            Analyzer analyzer=new Analyzer(activity,tts,data,gpsListener);
            analyzer.start();
        }
    }
}