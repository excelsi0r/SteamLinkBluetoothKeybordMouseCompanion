package nuno.steamlinkcontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
{
    Button connectButton;
    Button exitButton;
    TextView myMacView;
    BluetoothAdapter mBtAdapter;

    final int REQUEST_ENABLE_BT = 1;
    final int REQUEST_DEVICE_LIST = 2;
    final static String SAVED_DEVICE =  "SteamLinkDevice";
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final String UbuntuServerAddress = "A0:88:69:70:80:9F";
    private final String XUbuntuServerAddress = "00:19:0E:00:9E:C1";
    private final String SteamLinkServerAddress = "E0:31:9E:07:07:66";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectButton = findViewById(R.id.connectButton);
        exitButton = findViewById(R.id.exitButton);
        myMacView = findViewById(R.id.myMACviewText);

        //Checking Bluetooth Supported
        SpannableString content = null;
        if((mBtAdapter = BluetoothAdapter.getDefaultAdapter()) == null)
        {
            connectButton.setClickable(false);
            content = new SpannableString("Not Supported");
        }
        else
            content = new SpannableString(android.provider.Settings.Secure.getString(getContentResolver(), "bluetooth_address"));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        myMacView.setText(content);

        //set click listeners
        connectButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_DEVICE_LIST);
            }
        });


        exitButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        //Retrieve saved device to initiate auto-connect
        SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
        String address = mPrefs.getString(MainActivity.SAVED_DEVICE, null);
        if(address != null)
        {
            BluetoothDevice device = mBtAdapter.getRemoteDevice(address);

            //Verify if the device is available and possible to save and connect
            if(verifyDeviceAvailability(device))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.autoConnectDescription).setTitle(R.string.autoConnectTitle);

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO initialize connection
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                final AlertDialog dialog = builder.create();
                dialog.show();

                new CountDownTimer(5300, 500) {
                    public void onTick(long millisUntilFinished) {
                        dialog.setMessage(getString(R.string.autoConnectDescription) + " " + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
                        dialog.cancel();
                    }
                }.start();
            }
            else
            {
                //In case device is not available
                Toast.makeText(this, getString(R.string.savedDeviceNotAvailable), Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == REQUEST_DEVICE_LIST)
        {
            if(resultCode == RESULT_OK)
            {
                String address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                BluetoothDevice btDevice = mBtAdapter.getRemoteDevice(address);

                //Verify if the device is available and possible to save and connect
                if(verifyDeviceAvailability(btDevice))
                {
                    SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
                    mPrefs.edit().putString(MainActivity.SAVED_DEVICE, address).apply();

                    //TODO initialize connection
                }
                else
                {
                    Toast.makeText(this, getString(R.string.cannotSave), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void print(final String str)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                myMacView.append(str);
            }
        });
    }

    private class BluetoothTask extends AsyncTask<Void, Void, Integer>
    {
        @Override
        protected Integer doInBackground(Void... params)
        {
            BluetoothAdapter mBluetoothAdapter;
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (!mBluetoothAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            while (!mBluetoothAdapter.isEnabled()){}
/*
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0)
            {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices)
                {
                    String temp = "";
                    for (ParcelUuid uuid : device.getUuids())
                    {
                        temp += uuid.toString() + "\n";
                    }

                    Log.d("BLUETOOTHPAIRED", "===============================\n"
                     + device.getName() + "\n"
                     + device.getAddress() + "\n"
                     + device.getBluetoothClass().toString() + "\n"
                     + Integer.toString(device.getBondState()) + "\n"
                     + Integer.toString(device.getType()) + "\n"
                     + temp + "\n");

                }
            }
*/

            BluetoothDevice btDevice = mBluetoothAdapter.getRemoteDevice(SteamLinkServerAddress);

            print("Server Device Created\n");

            /*


*/

            BluetoothSocket btSocket = null;
            try
            {
                btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                print("Socket Created\n");
            }
            catch (IOException e)
            {
                print("ERROR: Failed to Create Socket!\n");
                return 1;
            }


            /*
                Connecting to socket
             */
            mBluetoothAdapter.cancelDiscovery();
            try
            {
                print("Attempting to connect...\n");
                btSocket.connect();
                print("Connected to Socket!\n");
            }
            catch (IOException e)
            {
                Log.d("BLUETOOTHTASK", e.toString());
                print("ERROR: Failed to Connect to Server Socket!\n");


                try
                {
                    btSocket =(BluetoothSocket) btDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(btDevice,11);
                    btSocket.connect();

                }
                catch (IOException e1)
                {
                    print("ERROR: Failed to Connect fallback socket!\n");
                    Log.d("BLUETOOTHTASK", e1.toString());
                    return 1;
                }
                catch (Exception n)
                {
                    print("ERROR: No such method!\n");
                    Log.d("BLUETOOTHTASK", n.toString());
                    return 1;
                }
            }


            /*
                Getting outputstream and writing
             */
            OutputStream outStream = null;
            try
            {
                print("Attempting to write...\n");
                outStream = btSocket.getOutputStream();
                outStream.write("hello!".getBytes());
                outStream.flush();
                print("Write Successful!\n");
            }
            catch (IOException e)
            {
                print("ERROR: Write to socket failed\n");
                return 1;
            }


            /*
                Closing the socket
             */
            try
            {
                print("Attempting to close socket...\n");
                btSocket.close();
                print("Socket closed\n");
            }
            catch (IOException e2)
            {
                print("ERROR: Failed to close sockets\n");
                return 1;
            }

            return 0;
        }
    }

    private Activity getActivity()
    {
        return this;
    }

    private void logDevice(BluetoothDevice btDevice)
    {
        Log.d("BLUETOOTHDEVICE", "===============================\n");
        Log.d("BLUETOOTHDEVICE",btDevice.getName() + "\n");
        Log.d("BLUETOOTHDEVICE",btDevice.getAddress() + "\n");
        Log.d("BLUETOOTHDEVICE",btDevice.getBluetoothClass().toString() + "\n");
        Log.d("BLUETOOTHDEVICE",Integer.toString(btDevice.getBondState()) + "\n");
        Log.d("BLUETOOTHDEVICE",Integer.toString(btDevice.getType()) + "\n");
    }

    private boolean verifyDeviceAvailability(BluetoothDevice device)
    {
        return device.getAddress() != null && device.getClass() != null;
    }
}
