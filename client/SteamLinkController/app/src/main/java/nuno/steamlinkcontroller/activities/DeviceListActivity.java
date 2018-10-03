package nuno.steamlinkcontroller.activities;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import nuno.steamlinkcontroller.R;

public class DeviceListActivity extends AppCompatActivity
{
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private  BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private Button scanButton;
    private Button cancelButton;
    private ProgressBar progressBar;
    private TextView availableText;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_list);
        setResult(Activity.RESULT_CANCELED);

        TextView pairedText = findViewById(R.id.pairedDevicesText);
        availableText = findViewById(R.id.availableDevicesText);
        scanButton = findViewById(R.id.scanDevicesButton);
        cancelButton = findViewById(R.id.cancelDevicesButton);
        progressBar = findViewById(R.id.progressBar);

        pairedText.setVisibility(View.GONE);
        availableText.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        scanButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startDiscovery();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopDiscovery();
            }
        });

        // Quick permission check
        int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        if (permissionCheck != 0) {

            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
        }

        //bluetooth adapter setup
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        //PAIRED DEVICES SETUP
        ArrayAdapter<String> pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        ListView pairedListView = findViewById(R.id.pairedDevicesList);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0)
        {
            findViewById(R.id.pairedDevicesText).setVisibility(View.VISIBLE);

            for (BluetoothDevice device : pairedDevices)
            {
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
        else
            findViewById(R.id.pairedDevicesText).setVisibility(View.GONE);

        //SCAN DEVICES SETUP
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        ListView scannedDevicesView = findViewById(R.id.availableDeviceList);
        scannedDevicesView.setAdapter(mNewDevicesArrayAdapter);
        scannedDevicesView.setOnItemClickListener(mDeviceClickListener);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        this.unregisterReceiver(mReceiver);
    }

    private void startDiscovery()
    {
        scanButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);

        mNewDevicesArrayAdapter.clear();
        availableText.setVisibility(View.VISIBLE);

        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        mBtAdapter.startDiscovery();
    }

    private void stopDiscovery()
    {
        scanButton.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        if(mNewDevicesArrayAdapter.getCount() < 1)
        {
            availableText.setVisibility(View.GONE);
            Toast.makeText(this, "No Devices Found", Toast.LENGTH_SHORT).show();
        }

        mBtAdapter.cancelDiscovery();
    }

    //this is an object not a function
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3)
        {
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED)
                {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    availableText.setVisibility(View.VISIBLE);
                }
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                stopDiscovery();
            }
        }
    };

}
