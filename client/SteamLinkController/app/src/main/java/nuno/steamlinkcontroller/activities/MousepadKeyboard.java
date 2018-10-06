package nuno.steamlinkcontroller.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.UUID;

import nuno.steamlinkcontroller.R;
import nuno.steamlinkcontroller.logic.OneFingerFSM;
import nuno.steamlinkcontroller.logic.OneFingerState;
import nuno.steamlinkcontroller.logic.TwoFingerFSM;
import nuno.steamlinkcontroller.logic.TwoFingerState;

import static android.view.KeyEvent.KEYCODE_DEL;
import static android.view.MotionEvent.*;

public class MousepadKeyboard extends AppCompatActivity
{
    private final int DELAY = 10;
    private final int MAX_RETRIES = 3;
    private final int REQUEST_ENABLE_BT = 1;
    private final static int RFCOMM_CHANNEL = 11;
    private int KEYB;
    private int MOUS;
    private int MWHL;

    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final String UbuntuServerAddress = "A0:88:69:70:80:9F";
    private final String XUbuntuServerAddress = "00:19:0E:00:9E:C1";
    private final String SteamLinkServerAddress = "E0:31:9E:07:07:66";

    //one finger mousepad
    private OneFingerFSM oneFingerFSM = new OneFingerFSM();
    private boolean drag1 = false;

    //two finger mousepad
    private TwoFingerFSM twoFingerFSM = new TwoFingerFSM();
    private boolean drag2 = false;

    //mouse up and down
    private boolean upMouse = false;
    private boolean downMouse = false;

    //Views for keyboard
    EditText dummyText;
    LinearLayout keyboardMouse;

    BluetoothAdapter mBtAdapter;
    BluetoothSocket btSocket = null;
    OutputStream btOutputStream = null;
    Resources res = null;

    @Override
    protected void onPause()
    {
        hideKeyboard();
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        try { btOutputStream.close(); } catch (Exception e) {}
        try { btSocket.close(); } catch (Exception e) {}

        finish();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mousepad_keyboard);

        keyboardMouse = findViewById(R.id.keyboardMouse);

        final ImageView img = findViewById(R.id.imageView);
        final Button leftButton = findViewById(R.id.left_mouse);
        final Button rightButton = findViewById(R.id.right_mouse);
        final Button upButton = findViewById(R.id.mouseUp);
        final Button downButton = findViewById(R.id.mouseDown);
        final ImageButton keyboardButton = findViewById(R.id.keyboardButton);
        dummyText = findViewById(R.id.dummyText);
        final Button f1Button = findViewById(R.id.f1);
        final Button f2Button = findViewById(R.id.f2);
        final Button f3Button = findViewById(R.id.f3);
        final Button f4Button = findViewById(R.id.f4);
        final Button f5Button = findViewById(R.id.f5);
        final Button f6Button = findViewById(R.id.f6);
        final Button f7Button = findViewById(R.id.f7);
        final Button f8Button = findViewById(R.id.f8);
        final Button f9Button = findViewById(R.id.f9);
        final Button escButton = findViewById(R.id.escButton);
        final Button homeButton = findViewById(R.id.homeButton);
        final Button pgUpButton = findViewById(R.id.pgUpButton);
        final Button delButton = findViewById(R.id.delButton);
        final Button tabButton = findViewById(R.id.tabButton);
        final Button endButton = findViewById(R.id.endButton);
        final Button pgDownButton = findViewById(R.id.pgDownButton);
        final Button insButton = findViewById(R.id.insButton);
        final Button ctrlButton = findViewById(R.id.ctrlButton);
        final ImageButton winButton = findViewById(R.id.windowsButton);
        final Button altButton = findViewById(R.id.altButton);
        final Button shiftButton = findViewById(R.id.shiftButton);
        final Button arrowUpButton = findViewById(R.id.arrowUp);
        final Button arrowDownButton = findViewById(R.id.arrowDown);
        final Button arrowLeftButton = findViewById(R.id.arrowLeft);
        final Button arrowRightButton = findViewById(R.id.arrowRight);

        KEYB = getResources().getInteger(R.integer.SLBC_KEYB);
        MOUS = getResources().getInteger(R.integer.SLBC_MOUS);
        MWHL = getResources().getInteger(R.integer.SLBC_MWHL);

        res = getResources();


        final Handler handler=new Handler();
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {

                OneFingerState state = oneFingerFSM.getEventToSend();
                switch (state)
                {
                    case FU1:
                        mouseOneFingerOneTap();
                        break;
                    case SD1:
                        mouseOneFingerDrag();
                        drag1 = true;
                        break;
                    case SU1:
                        mouseOneFingerDoubleTap();
                }

                TwoFingerState state2 = twoFingerFSM.getEventToSend();
                switch (state2)
                {
                    case FD2:
                        drag2 = true;
                        mouseTwoFingerDrag();
                        break;
                    case FU2:
                        mouseTwoFingerOneTap();
                        break;
                }


                if(upMouse)
                {
                    btnMouseWheelUp();
                }

                if(downMouse)
                {
                    btnMouseWheelDown();
                }

                handler.postDelayed(this,DELAY);
            }
        });

        img.setOnTouchListener(new View.OnTouchListener()
        {
            int posX = 0;
            int posY = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                switch (motionEvent.getAction() & ACTION_MASK)
                {
                    case (ACTION_DOWN):
                        oneFingerFSM.updateState(true);
                        break;

                    case (ACTION_UP):
                        if(drag1)
                        {
                            drag1 = false;
                            mouseOneFingerRelease();
                        }
                        else
                            oneFingerFSM.updateState(false);

                        break;

                    case (ACTION_MOVE):
                        int diffX = (int) motionEvent.getX() - posX;
                        int diffY = (int) motionEvent.getY() - posY;

                        if(motionEvent.getPointerCount() == 1 && (diffX != 0 || diffY != 0))
                        {
                            mouseOneFingerMovement(diffX, diffY);
                        }
                        else if(motionEvent.getPointerCount() > 1 && diffY != 0)
                        {
                            mouseTwoFingerMovement(diffY);
                        }

                        break;
                    case (ACTION_POINTER_DOWN):
                        twoFingerFSM.updateState(true);
                        break;
                    case(ACTION_POINTER_UP):
                        if(drag2)
                        {
                            drag2 = false;
                            mouseTwoFingerRelease();
                        }
                        else
                            twoFingerFSM.updateState(false);
                        break;
                }

                posX = (int) motionEvent.getX();
                posY = (int) motionEvent.getY();

                return true;
            }
        });


        leftButton.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                switch (motionEvent.getAction() & ACTION_MASK)
                {
                    case (ACTION_DOWN):
                        btnMouseLeftDown();
                        break;

                    case (ACTION_UP):
                        btnMouseLeftUp();
                        break;
                }
                return true;
            }
        });

        rightButton.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                switch (motionEvent.getAction() & ACTION_MASK)
                {
                    case (ACTION_DOWN):
                        btnMouseRightDown();
                        break;

                    case (ACTION_UP):
                        btnMouseRightUp();

                        break;
                }

                return true;
            }
        });

        upButton.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                switch (motionEvent.getAction() & ACTION_MASK)
                {
                    case (ACTION_DOWN):
                        upMouse = true;
                        btnMouseUpDown();
                        break;

                    case (ACTION_UP):
                        upMouse = false;
                        btnMouseUpUp();
                        break;
                }
                return false;
            }
        });

        downButton.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                switch (motionEvent.getAction() & ACTION_MASK)
                {
                    case (ACTION_DOWN):
                        downMouse = true;
                        btnMouseDownDown();
                        break;

                    case (ACTION_UP):
                        downMouse = false;
                        btnMouseDownUp();
                        break;
                }

                return false;
            }
        });

        keyboardButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showKeyboard();
            }
        });

        dummyText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count)
            {
                if(count > before && count == 1)
                    key(charSequence.charAt(start));
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        dummyText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent)
            {
                switch (keyEvent.getKeyCode())
                {
                    case KEYCODE_DEL:
                        if(keyEvent.getAction() == ACTION_DOWN)
                            keyBackspace(true);
                        else
                            keyBackspace(false);
                }
                return false;
            }
        });

        dummyText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
            {
                if((keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_PREVIOUS))
                    keyEnter();
                return false;
            }
        });

        f1Button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyf1(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyf1(false);
                return true;
            }
        });

        f2Button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyf2(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyf2(false);
                return true;
            }
        });

        f3Button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyf3(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyf3(false);
                return true;
            }
        });

        f4Button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyf4(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyf4(false);
                return true;
            }
        });

        f5Button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyf5(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyf5(false);
                return true;
            }
        });

        f6Button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyf6(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyf6(false);
                return true;
            }
        });

        f7Button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyf7(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyf7(false);
                return true;
            }
        });

        f8Button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyf8(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyf8(false);
                return true;
            }
        });

        f9Button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyf9(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyf9(false);
                return true;
            }
        });

        escButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyEsc(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyEsc(false);
                return true;
            }
        });

        homeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyHome(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyHome(false);
                return true;
            }
        });


        pgUpButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyPgUp(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyPgUp(false);
                return true;
            }
        });

        delButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyDel(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyDel(false);
                return true;
            }
        });

        tabButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyTab(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyTab(false);
                return true;
            }
        });

        endButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyEnd(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyEnd(false);
                return true;
            }
        });

        pgDownButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyPgDown(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyPgDown(false);
                return true;
            }
        });

        insButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyIns(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyIns(false);
                return true;
            }
        });

        ctrlButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyCtrl(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyCtrl(false);
                return true;
            }
        });

        winButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyWin(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyWin(false);
                return true;
            }
        });

        altButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyAlt(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyAlt(false);
                return true;
            }
        });

        shiftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyShift(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyShift(false);
                return true;
            }
        });

        arrowUpButton.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyArrowUp(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyArrowUp(false);
                return true;
            }
        });


        arrowDownButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyArrowDown(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyArrowDown(false);
                return true;
            }
        });


        arrowLeftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyArrowLeft(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyArrowLeft(false);
                return true;
            }
        });


        arrowRightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyArrowRight(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyArrowRight(false);
                return true;
            }
        });

        BluetoothDevice btDevice = getIntent().getExtras().getParcelable(MainActivity.BLUETOOTH_DEVICE_EXTRA);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        MousepadKeyboard.AttemptConnection connection = new MousepadKeyboard.AttemptConnection();
        connection.execute(btDevice);
    }

    /**
       19 different events
     */

    private void mouseOneFingerOneTap()
    {
        //send mouse btnLeft down, upb
        send( KEYB + " " + res.getInteger(R.integer.SLBC_BTN_LEFT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
        send( KEYB + " " + res.getInteger(R.integer.SLBC_BTN_LEFT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
        Log.d("EVENTCODE", 1 + "");
    }

    private void mouseOneFingerDrag()
    {
        //send mouse btnLeft down
        send( KEYB + " " + res.getInteger(R.integer.SLBC_BTN_LEFT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
        Log.d("EVENTCODE", 2 + "");
    }

    private void mouseOneFingerRelease()
    {
        //send mouse btnLeft up
        send( KEYB + " " + res.getInteger(R.integer.SLBC_BTN_LEFT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
        Log.d("EVENTCODE", 3 + "");
    }

    private void mouseOneFingerDoubleTap()
    {
        //send mouse btnLeft down, up, down, up
        send( KEYB + " " + res.getInteger(R.integer.SLBC_BTN_LEFT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
        send( KEYB + " " + res.getInteger(R.integer.SLBC_BTN_LEFT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
        send( KEYB + " " + res.getInteger(R.integer.SLBC_BTN_LEFT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
        send( KEYB + " " + res.getInteger(R.integer.SLBC_BTN_LEFT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
        Log.d("EVENTCODE",  4 + "");
    }

    private void mouseOneFingerMovement(int x, int y)
    {
        //send mouse move x and y
        send( MOUS + " " + x + " " + y);
        Log.d("EVENTCODE", 5 + "");
    }

    private void mouseTwoFingerOneTap()
    {
        //send mouse btnRight down, up
        send( KEYB + " " + res.getInteger(R.integer.SLBC_BTN_RIGHT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
        send( KEYB + " " + res.getInteger(R.integer.SLBC_BTN_RIGHT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
        Log.d("EVENTCODE", 6 + "");
    }

    private void mouseTwoFingerDrag()
    {
        //DEPRECATED
        //NOT NEEDED, MOUSE TO MOUSEPAD CONVERSION DOES NOT EXIST
        //send two finger down
        Log.d("EVENTCODE", 7 + "");
    }

    private void mouseTwoFingerRelease()
    {
        //DEPRECATED
        //NOT NEEDED, MOUSE TO MOUSEPAD CONVERSION DOES NOT EXIST
        //send two finger up
        Log.d("EVENTCODE", 8 + "");
    }

    private void mouseTwoFingerMovement(int y)
    {
        //send rel mouse wheel y event
        send( MWHL + " " + y + " " + 0);
        Log.d("EVENTCODE", 9+ "");
    }

    private void btnMouseWheelUp()
    {
        //send rel mouse wheel vertical up
        send( MWHL + " " + res.getInteger(R.integer.SLBC_MWHL_UP_COUNT) + " " + 0);
        Log.d("EVENTCODE", 10 + "");
    }

    private void btnMouseWheelDown()
    {
        //send rel mouse vertical down
        send( MWHL + " " + res.getInteger(R.integer.SLBC_MWHL_DN_COUNT) + " " + 0);
        Log.d("EVENTCODE", 11 + "");
    }

    private void btnMouseLeftDown()
    {
        //send left mouse down
        send( KEYB + " " + res.getInteger(R.integer.SLBC_BTN_LEFT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
        Log.d("EVENTCODE", 12 + "");
    }

    private void btnMouseLeftUp()
    {
        //send left mouse up
        send( KEYB + " " + res.getInteger(R.integer.SLBC_BTN_LEFT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
        Log.d("EVENTCODE", 13 + "");
    }

    private void btnMouseRightDown()
    {
        //send right mouse down
        send( KEYB + " " + res.getInteger(R.integer.SLBC_BTN_RIGHT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
        Log.d("EVENTCODE", 14 + "");
    }

    private void btnMouseRightUp()
    {
        //send right mouse up
        send( KEYB + " " + res.getInteger(R.integer.SLBC_BTN_RIGHT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
        Log.d("EVENTCODE", 15 + "");
    }

    private void btnMouseUpDown()
    {
        //DEPRECATED
        //NO NEED
        //send up mouse down
        Log.d("EVENTCODE", 16 + "");
    }

    private void btnMouseUpUp()
    {
        //DEPRECATED
        //NO NEED
        //send up mouse up
        Log.d("EVENTCODE", 17 + "");
    }

    private void btnMouseDownDown()
    {
        //DEPRECATED
        //NO NEED
        //send down mouse down
        Log.d("EVENTCODE", 18 + "");
    }

    private void btnMouseDownUp()
    {
        //DEPRECATED
        //NO NEED
        //send down mouse up
        Log.d("EVENTCODE", 19 + "");
    }

    private void keyEnter()
    {
        //send enter down, up
        send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_ENTER) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
        send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_ENTER) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
        Log.d("EVENTCODE", 20 + "");
    }

    private void keyBackspace(boolean down)
    {
        if(down)
        {
            //send backspace down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_BACKSPACE) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 21 + "");
        }
        else
        {
            //send backspace up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_BACKSPACE) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 22 + "");
        }
    }

    private void key(char key)
    {
        //TODO map key to linux integer and send
        //this shall be a lot of fun

        switch (key)
        {
            case 'q': sendDownUp(res.getInteger(R.integer.SLBC_KEY_Q)); break;
            case 'w': sendDownUp(res.getInteger(R.integer.SLBC_KEY_W)); break;
            case 'e': sendDownUp(res.getInteger(R.integer.SLBC_KEY_E)); break;
            case 'r': sendDownUp(res.getInteger(R.integer.SLBC_KEY_R)); break;
            case 't': sendDownUp(res.getInteger(R.integer.SLBC_KEY_T)); break;
            case 'y': sendDownUp(res.getInteger(R.integer.SLBC_KEY_Y)); break;
            case 'u': sendDownUp(res.getInteger(R.integer.SLBC_KEY_U)); break;
            case 'i': sendDownUp(res.getInteger(R.integer.SLBC_KEY_I)); break;
            case 'o': sendDownUp(res.getInteger(R.integer.SLBC_KEY_O)); break;
            case 'p': sendDownUp(res.getInteger(R.integer.SLBC_KEY_P)); break;
            case 'a': sendDownUp(res.getInteger(R.integer.SLBC_KEY_A)); break;
            case 's': sendDownUp(res.getInteger(R.integer.SLBC_KEY_S)); break;
            case 'd': sendDownUp(res.getInteger(R.integer.SLBC_KEY_D)); break;
            case 'f': sendDownUp(res.getInteger(R.integer.SLBC_KEY_F)); break;
            case 'g': sendDownUp(res.getInteger(R.integer.SLBC_KEY_G)); break;
            case 'h': sendDownUp(res.getInteger(R.integer.SLBC_KEY_H)); break;
            case 'j': sendDownUp(res.getInteger(R.integer.SLBC_KEY_J)); break;
            case 'k': sendDownUp(res.getInteger(R.integer.SLBC_KEY_K)); break;
            case 'l': sendDownUp(res.getInteger(R.integer.SLBC_KEY_L)); break;
            case 'z': sendDownUp(res.getInteger(R.integer.SLBC_KEY_Z)); break;
            case 'x': sendDownUp(res.getInteger(R.integer.SLBC_KEY_X)); break;
            case 'c': sendDownUp(res.getInteger(R.integer.SLBC_KEY_C)); break;
            case 'v': sendDownUp(res.getInteger(R.integer.SLBC_KEY_V)); break;
            case 'b': sendDownUp(res.getInteger(R.integer.SLBC_KEY_B)); break;
            case 'n': sendDownUp(res.getInteger(R.integer.SLBC_KEY_N)); break;
            case 'm': sendDownUp(res.getInteger(R.integer.SLBC_KEY_M)); break;

            case 'Q': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_Q)); break;
            case 'W': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_W)); break;
            case 'E': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_E)); break;
            case 'R': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_R)); break;
            case 'T': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_T)); break;
            case 'Y': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_Y)); break;
            case 'U': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_U)); break;
            case 'I': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_I)); break;
            case 'O': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_O)); break;
            case 'P': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_P)); break;
            case 'A': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_A)); break;
            case 'S': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_S)); break;
            case 'D': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_D)); break;
            case 'F': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_F)); break;
            case 'G': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_G)); break;
            case 'H': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_H)); break;
            case 'J': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_J)); break;
            case 'K': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_K)); break;
            case 'L': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_L)); break;
            case 'Z': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_Z)); break;
            case 'X': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_X)); break;
            case 'C': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_C)); break;
            case 'V': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_V)); break;
            case 'B': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_B)); break;
            case 'N': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_N)); break;
            case 'M': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_M)); break;

            case '0': sendDownUp(res.getInteger(R.integer.SLBC_KEY_0)); break;
            case '1': sendDownUp(res.getInteger(R.integer.SLBC_KEY_1)); break;
            case '2': sendDownUp(res.getInteger(R.integer.SLBC_KEY_2)); break;
            case '3': sendDownUp(res.getInteger(R.integer.SLBC_KEY_3)); break;
            case '4': sendDownUp(res.getInteger(R.integer.SLBC_KEY_4)); break;
            case '5': sendDownUp(res.getInteger(R.integer.SLBC_KEY_5)); break;
            case '6': sendDownUp(res.getInteger(R.integer.SLBC_KEY_6)); break;
            case '7': sendDownUp(res.getInteger(R.integer.SLBC_KEY_7)); break;
            case '8': sendDownUp(res.getInteger(R.integer.SLBC_KEY_8)); break;
            case '9': sendDownUp(res.getInteger(R.integer.SLBC_KEY_9)); break;

            case '+': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_EQUAL)); break;
            case '=': sendDownUp(res.getInteger(R.integer.SLBC_KEY_EQUAL)); break;
            case '%': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_5)); break;
            case '/': sendDownUp(res.getInteger(R.integer.SLBC_KEY_SLASH)); break;
            case '\\':sendDownUp(res.getInteger(R.integer.SLBC_KEY_BACKSLASH)); break;
            case '$': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_4)); break;
            case '€': sendAltGr(res.getInteger(R.integer.SLBC_KEY_4)); break;
            case '£': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_3)); break;
            case '@': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_APOSTROPHE)); break;
            case '*': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_8)); break;
            case '!': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_1)); break;
            case '#': sendDownUp(res.getInteger(R.integer.SLBC_KEY_GRAVE)); break;
            case ':': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_SEMICOLON)); break;
            case ';': sendDownUp(res.getInteger(R.integer.SLBC_KEY_SEMICOLON)); break;
            case '&': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_7)); break;
            case '_': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_MINUS)); break;
            case '(': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_9)); break;
            case ')': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_0)); break;
            case '-': sendDownUp(res.getInteger(R.integer.SLBC_KEY_MINUS)); break;
            case '\'':sendDownUp(res.getInteger(R.integer.SLBC_KEY_APOSTROPHE)); break;
            case '"': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_2)); break;
            case ',': sendDownUp(res.getInteger(R.integer.SLBC_KEY_COMMA)); break;
            case '.': sendDownUp(res.getInteger(R.integer.SLBC_KEY_DOT)); break;
            case '?': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_SLASH)); break;
            case '^': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_6)); break;
            case '[': sendDownUp(res.getInteger(R.integer.SLBC_KEY_LEFTBRACE)); break;
            case ']': sendDownUp(res.getInteger(R.integer.SLBC_KEY_RIGHTBRACE)); break;
            case '<': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_COMMA)); break;
            case '>': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_DOT)); break;
            case '`': sendDownUp(res.getInteger(R.integer.SLBC_KEY_102ND)); break;
            case '¬': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_102ND)); break;
            case '{': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_LEFTBRACE)); break;
            case '}': sendUpperCase(res.getInteger(R.integer.SLBC_KEY_RIGHTBRACE)); break;
            case '|': sendAltGr(res.getInteger(R.integer.SLBC_KEY_102ND)); break;
        }

        Log.d("EVENTCODE", 23 + " '" + key + "'");
    }

    private void keyf1(boolean down)
    {
        if(down) {
            //send f1 down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_F1) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 24 + ""); }
        else {
            //send f1 up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_F1) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 25 + ""); }
    }

    private void keyf2(boolean down)
    {
        if(down) {
            //send f2 down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_F2) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 26 + ""); }
        else {
            //send f2 up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_F2) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 27 + ""); }
    }

    private void keyf3(boolean down)
    {
        if(down) {
            //send f3 down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_F3) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 28 + ""); }
        else {
            //send f3 up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_F3) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 29 + ""); }
    }


    private void keyf4(boolean down)
    {
        if(down) {
            //send f4 down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_F4) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 30 + ""); }
        else {
            //send f4 up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_F4) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 31 + ""); }
    }

    private void keyf5(boolean down)
    {
        if(down) {
            //send f5 down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_F5) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 32 + ""); }
        else {
            //send f5 up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_F5) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 33 + ""); }
    }


    private void keyf6(boolean down)
    {
        if(down) {
            //send f6 down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_F6) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 34 + ""); }
        else {
            //send f6 up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_F6) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 35 + ""); }
    }

    private void keyf7(boolean down)
    {
        if(down) {
            //send f7 down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_F7) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 36 + ""); }
        else {
            //send f7 up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_F7) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 37 + ""); }
    }

    private void keyf8(boolean down)
    {
        if(down) {
            //send f8 down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_F8) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 38 + ""); }
        else {
            //send f8 up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_F8) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 39 + ""); }
    }

    private void keyf9(boolean down)
    {
        if(down) {
            //send f9 down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_F9) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 40 + ""); }
        else {
            //send f9 up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_F9) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 41 + ""); }
    }

    private void keyEsc(boolean down)
    {
        if(down) {
            //send esc down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_ESC) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 42 + ""); }
        else {
            //send esc up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_ESC) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 43 + ""); }
    }
    private void keyHome(boolean down)
    {
        if(down) {
            //send home down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_HOME) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 44 + ""); }
        else {
            //send home up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_HOME) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 45 + ""); }
    }

    private void keyPgUp(boolean down)
    {
        if(down) {
            //send PgUp down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_PAGEUP) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 46 + ""); }
        else {
            //send PgUp up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_PAGEUP) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 47 + ""); }
    }

    private void keyPgDown(boolean down)
    {
        if(down) {
            //send PgDn down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_PAGEDOWN) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 48 + ""); }
        else {
            // send PgDn up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_PAGEDOWN) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 49 + ""); }
    }

    private void keyDel(boolean down)
    {
        if(down) {
            //send del down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_DELETE) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 50 + ""); }
        else {
            //send del up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_DELETE) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 51 + ""); }
    }

    private void keyTab(boolean down)
    {
        if(down) {
            //send tab down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_TAB) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 52 + ""); }
        else {
            //send tab up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_TAB) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 53 + ""); }
    }

    private void keyEnd(boolean down)
    {
        if(down) {
            //send end down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_END) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 54 + ""); }
        else {
            //send end up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_END) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 55 + ""); }
    }

    private void keyIns(boolean down)
    {
        if(down) {
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_INSERT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 56 + ""); }
        else {
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_INSERT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 57 + ""); }
    }

    private void keyCtrl(boolean down)
    {
        if(down) {
            //send ctrl down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_LEFTCTRL) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 58 + ""); }
        else {
            //send ctrl up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_LEFTCTRL) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 59 + ""); }
    }

    private void keyWin(boolean down)
    {
        if(down) {
            //send win down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_LEFTMETA) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 60 + ""); }
        else {
            //send win up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_LEFTMETA) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 61 + ""); }
    }

    private void keyAlt(boolean down)
    {
        if(down) {
            //send alt down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_LEFTALT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 62 + ""); }
        else {
            //send alt up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_LEFTALT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 63 + ""); }
    }

    private void keyShift(boolean down)
    {
        if(down) {
            //send shift down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_LEFTSHIFT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 64 + ""); }
        else {
            //send shift up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_LEFTSHIFT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 65 + ""); }
    }

    private void keyArrowUp(boolean down)
    {
        if(down) {
            //send arrowUp down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_UP) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 66 + ""); }
        else {
            //send arrowUp up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_UP) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 67 + ""); }
    }

    private void keyArrowDown(boolean down)
    {
        if(down) {
            //send arrowDown down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_DOWN) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 68 + ""); }
        else {
            //send arrowDown up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_DOWN) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 69 + ""); }
    }

    private void keyArrowLeft(boolean down)
    {
        if(down) {
            //send arrowLeft down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_LEFT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 70 + ""); }
        else {
            //send arrowLeft up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_LEFT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 71 + ""); }
    }

    private void keyArrowRight(boolean down)
    {
        if(down) {
            //send arrowLeft down
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_RIGHT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
            Log.d("EVENTCODE", 72 + ""); }
        else {
            //send arrowLeft up
            send( KEYB + " " + res.getInteger(R.integer.SLBC_KEY_RIGHT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
            Log.d("EVENTCODE", 73 + ""); }
    }

    private void showKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(dummyText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard()
    {

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(dummyText.getWindowToken(), 0);
    }

    private class AttemptConnection extends AsyncTask<BluetoothDevice, Integer, BluetoothSocket>
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
                Toast.makeText(MousepadKeyboard.this, getString(R.string.failedToConnect), Toast.LENGTH_SHORT).show();
                finish();
            }
            else
            {
                keyboardMouse.setVisibility(View.VISIBLE);
                btSocket = bluetoothSocket;
                try
                {
                    OutputStream outputStream = bluetoothSocket.getOutputStream();
                    btOutputStream = outputStream;
                }
                catch (Exception e)
                {
                    Toast.makeText(MousepadKeyboard.this, getString(R.string.failedToConnect), Toast.LENGTH_SHORT).show();
                    finish();
                }
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
            while (!mBtAdapter.isEnabled() && !timeout){}

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
                    btSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, MousepadKeyboard.RFCOMM_CHANNEL);
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

    public void toast()
    {
        Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
    }

    private void sendDownUp(int keyCode)
    {
        send(KEYB + " " + keyCode + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
        send(KEYB + " " + keyCode + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
    }

    private void sendUpperCase(int keyCode)
    {
        send(KEYB + " " + res.getInteger(R.integer.SLBC_KEY_LEFTSHIFT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
        send(KEYB + " " + keyCode + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
        send(KEYB + " " + keyCode + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
        send(KEYB + " " + res.getInteger(R.integer.SLBC_KEY_LEFTSHIFT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
    }

    private void sendAltGr(int keyCode)
    {
        send(KEYB + " " + res.getInteger(R.integer.SLBC_KEY_RIGHTALT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
        send(KEYB + " " + keyCode + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_DOWN));
        send(KEYB + " " + keyCode + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
        send(KEYB + " " + res.getInteger(R.integer.SLBC_KEY_RIGHTALT) + " " + res.getInteger(R.integer.SLBC_KEY_ACTION_UP));
    }

    private void send(String str)
    {
        if(btOutputStream != null)
        {
            try
            {
                btOutputStream.write(str.getBytes());
                btOutputStream.flush();
            }
            catch (Exception e) {}
        }
    }
}
