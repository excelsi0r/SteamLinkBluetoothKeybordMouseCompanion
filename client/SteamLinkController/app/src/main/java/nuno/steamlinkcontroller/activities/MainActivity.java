package nuno.steamlinkcontroller.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import nuno.steamlinkcontroller.R;

public class MainActivity extends AppCompatActivity
{
    Button connectButton;
    Button exitButton;
    TextView myMacView;
    BluetoothAdapter mBtAdapter;

    final static String BLUETOOTH_DEVICE_EXTRA = "BluetoothDeviceExtra";
    final static String SAVED_DEVICE =  "SteamLinkDevice";
    final int REQUEST_ENABLE_BT = 1;
    final int REQUEST_DEVICE_LIST = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectButton = findViewById(R.id.connectButton);
        exitButton = findViewById(R.id.exitButton);
        myMacView = findViewById(R.id.myMACviewText);
        final TextView linkTextView = findViewById(R.id.linkTextView);

        //Checking Bluetooth Supported
        SpannableString content = null;
        if((mBtAdapter = BluetoothAdapter.getDefaultAdapter()) == null)
        {
            connectButton.setClickable(false);
            content = new SpannableString("Not Supported");
        }
        else
        {
            if (!mBtAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            content = new SpannableString(android.provider.Settings.Secure.getString(getContentResolver(), "bluetooth_address"));
        }
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
            final BluetoothDevice device = mBtAdapter.getRemoteDevice(address);

            //Verify if the device is available and possible to save and connect
            if(verifyDeviceAvailability(device))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.autoConnectDescription).setTitle(R.string.autoConnectTitle);

                //setting buttons and listeners
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        connect(device);
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);

                final AlertDialog dialog = builder.create();
                dialog.show();

                //countdown timer, in order to autoConnect after 5seconds, if user hasn't cancelled
                new CountDownTimer(5300, 500) {
                    public void onTick(long millisUntilFinished)
                    {
                        dialog.setMessage(getString(R.string.autoConnectDescription) + " " + millisUntilFinished / 1000);
                    }
                    public void onFinish()
                    {
                        if(dialog.isShowing())
                        {
                            dialog.dismiss();
                            connect(device);
                        }
                    }
                }.start();
            }
            else
            {
                //In case device is not available
                Toast.makeText(this, getString(R.string.savedDeviceNotAvailable), Toast.LENGTH_SHORT).show();
            }
        }

        Spanned html = Html.fromHtml("<a href='" + getResources().getString(R.string.githubPage) + "'>Server Setup</a>");

        linkTextView.setMovementMethod(LinkMovementMethod.getInstance());

        linkTextView.setText(html);
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

                    connect(btDevice);
                }
                else
                {
                    Toast.makeText(this, getString(R.string.cannotSave), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void connect(BluetoothDevice device)
    {
        Intent intent = new Intent(getActivity(), MousepadKeyboard.class);
        intent.putExtra(BLUETOOTH_DEVICE_EXTRA, device);
        startActivity(intent);
    }

    private Activity getActivity()
    {
        return this;
    }

    private boolean verifyDeviceAvailability(BluetoothDevice device)
    {
        return device.getAddress() != null && device.getClass() != null;
    }
}
