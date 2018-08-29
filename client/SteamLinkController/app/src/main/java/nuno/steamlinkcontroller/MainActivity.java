package nuno.steamlinkcontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
{
    Button connectButton;
    Button editButton;
    Button exitButton;
    TextView myMacView;


    final int REQUEST_ENABLE_BT = 1;
    final int REQUEST_DEVICE_LIST = 2;
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
        editButton = findViewById(R.id.editButton);
        exitButton = findViewById(R.id.exitButton);
        myMacView = findViewById(R.id.myMACviewText);

        SpannableString content = null;

        if(BluetoothAdapter.getDefaultAdapter() == null)
            content = new SpannableString("Not Supported");
        else
            content = new SpannableString(android.provider.Settings.Secure.getString(getContentResolver(), "bluetooth_address"));

        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        myMacView.setText(content);

        connectButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);

                Gson gson = new Gson();
                String json = mPrefs.getString("SteamLinkDevice", null);

                if(json == null)
                {
                    Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_DEVICE_LIST);
                }
                else
                {
                    BluetoothDevice btDevice = gson.fromJson(json, BluetoothDevice.class);
                    //connect with retrieved device
                }
            }
        });

        editButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
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
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == REQUEST_ENABLE_BT)
        {

        }
        else if(requestCode == REQUEST_DEVICE_LIST)
        {
            if(resultCode == RESULT_OK)
            {
                String address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                Log.d("BLUETOOTHTASK", address);

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
            String temp = "";
            for (ParcelUuid uuid : btDevice.getUuids())
            {
                temp += uuid.toString() + "\n";
            }

            Log.d("BLUETOOTHPAIRED", "===============================\n"
                    + btDevice.getName() + "\n"
                    + btDevice.getAddress() + "\n"
                    + btDevice.getBluetoothClass().toString() + "\n"
                    + Integer.toString(btDevice.getBondState()) + "\n"
                    + Integer.toString(btDevice.getType()) + "\n"
                    + temp + "\n");

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
}
