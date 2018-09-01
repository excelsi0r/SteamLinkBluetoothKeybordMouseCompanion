package nuno.steamlinkcontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class KeyboardMouseActivity extends AppCompatActivity
{
    BluetoothAdapter mBtAdapter;

    final int MAX_RETRIES = 3;
    final int REQUEST_ENABLE_BT = 1;
    final static int RFCOMM_CHANNEL = 11;


    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final String UbuntuServerAddress = "A0:88:69:70:80:9F";
    private final String XUbuntuServerAddress = "00:19:0E:00:9E:C1";
    private final String SteamLinkServerAddress = "E0:31:9E:07:07:66";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard_mouse);

        BluetoothDevice btDevice = getIntent().getExtras().getParcelable(MainActivity.BLUETOOTH_DEVICE_EXTRA);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        AttemptConnection connection = new AttemptConnection();
        connection.execute(btDevice);
    }

    private class AttemptConnection extends AsyncTask<BluetoothDevice, Void, BluetoothSocket>
    {
        boolean timeout = false;
        AlertDialog dialog;
        CountDownTimer countDownTimer;

        @Override
        protected void onPreExecute()
        {
            countDownTimer  = new CountDownTimer(10500, 500)
            {
                @Override
                public void onTick(long l) { }

                @Override
                public void onFinish() { timeout = true; }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface)
                {
                    finish();
                    cancel(true);
                }
            });
            dialog = builder.create();
            dialog.setMessage(getString(R.string.connectingText));


            LayoutInflater inflater = getActivity().getLayoutInflater();
            View content = inflater.inflate(R.layout.activity_connecting, null);
            dialog.setView(content);
            dialog.show();
        }

        @Override
        protected void onPostExecute(BluetoothSocket bluetoothSocket)
        {
            dialog.dismiss();

            if(bluetoothSocket == null)
            {
                Toast.makeText(KeyboardMouseActivity.this, getString(R.string.failedToConnect), Toast.LENGTH_SHORT).show();
                finish();
            }
            else
            {
                //TODO for testing purposes send hello
                try
                {
                    OutputStream outputStream = bluetoothSocket.getOutputStream();
                    outputStream.write("hello".getBytes());
                    outputStream.flush();
                    bluetoothSocket.close();
                    finish();
                }
                catch (Exception e) { }

                //TODO get output stream and set listeners
            }

        }

        @Override
        protected BluetoothSocket doInBackground(BluetoothDevice... btDeviceArray)
        {
            //socket to return
            BluetoothSocket btSocket = null;
            BluetoothDevice device = btDeviceArray[0];

            //attempt to enable bluetooth... again
            if (!mBtAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            //wait until adapter is connected, 10 second timeout
            countDownTimer.start();

            //continue only if bluetoothadapter is enabled && if timeout hasn't ended
            while (!mBtAdapter.isEnabled() || !timeout){}

            //final check if timeout occurred
            if(!mBtAdapter.isEnabled())
                return null;

            boolean connected = false;
            int retries = 0;

            //attempting to connect to failSafe socket
            while (retries <= MAX_RETRIES && !connected)
            {
                try
                {
                    btSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, KeyboardMouseActivity.RFCOMM_CHANNEL);
                    btSocket.connect();
                    connected = true;

                }
                catch (Exception n)
                {
                    connected = false;
                    retries++;
                }
            }

            if(!connected)
                return null;
            else
                return btSocket;
        }
    }

    private Activity getActivity()
    {
        return this;
    }
}
