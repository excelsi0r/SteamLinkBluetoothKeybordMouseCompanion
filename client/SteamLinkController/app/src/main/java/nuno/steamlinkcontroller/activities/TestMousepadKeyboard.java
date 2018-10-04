package nuno.steamlinkcontroller.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import nuno.steamlinkcontroller.R;
import nuno.steamlinkcontroller.logic.OneFingerFSM;
import nuno.steamlinkcontroller.logic.OneFingerState;
import nuno.steamlinkcontroller.logic.TwoFingerFSM;
import nuno.steamlinkcontroller.logic.TwoFingerState;

import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_DEL;
import static android.view.MotionEvent.*;

public class TestMousepadKeyboard extends AppCompatActivity
{
    private int MAX_DELTA;
    private final int DELAY = 10;

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

    @Override
    protected void onPause()
    {
        hideKeyboard();
        super.onPause();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mousepad_keyboard);

        final ImageView img = findViewById(R.id.imageView);
        final Button leftButton = findViewById(R.id.left_mouse);
        final Button rightButton = findViewById(R.id.right_mouse);
        final Button upButton = findViewById(R.id.mouseUp);
        final Button downButton = findViewById(R.id.mouseDown);
        final ImageButton keyboardButton = findViewById(R.id.keyboardButton);
        dummyText = findViewById(R.id.dummyText);

        MAX_DELTA = getResources().getInteger(R.integer.MAX_DELTA);

        final Handler handler=new Handler();
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                final long curr = System.currentTimeMillis();

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
                long curr = System.currentTimeMillis();

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


    }

    public void toast()
    {
        Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
    }

    /**
       19 different events
     */

    private void mouseOneFingerOneTap()
    {
        //TODO send down, up
        Log.d("EVENTCODE", 1 + "");
    }

    private void mouseOneFingerDrag()
    {
        //TODO send down
        Log.d("EVENTCODE", 2 + "");
    }

    private void mouseOneFingerRelease()
    {
        //TODO send up
        Log.d("EVENTCODE", 3 + "");
    }

    private void mouseOneFingerDoubleTap()
    {
        //TODO send down, up, down, up
        Log.d("EVENTCODE",  4 + "");
    }

    private void mouseOneFingerMovement(int x, int y)
    {
        //TODO send move x and y
        Log.d("EVENTCODE", 5 + "");
    }

    private void mouseTwoFingerOneTap()
    {
        //TODO send two finger down, up
        Log.d("EVENTCODE", 6 + "");
    }

    private void mouseTwoFingerDrag()
    {
        //TODO send two finger down
        Log.d("EVENTCODE", 7 + "");
    }

    private void mouseTwoFingerRelease()
    {
        //TODO send two finger up
        Log.d("EVENTCODE", 8 + "");
    }

    private void mouseTwoFingerMovement(int y)
    {
        //TODO send rel wheel y event
        Log.d("EVENTCODE", 9+ "");
    }

    private void btnMouseWheelUp()
    {
        //TODO send rel vertical mouse up
        Log.d("EVENTCODE", 10 + "");
    }

    private void btnMouseWheelDown()
    {
        //TODO send rel vertical mouse down
        Log.d("EVENTCODE", 11 + "");
    }

    private void btnMouseLeftDown()
    {
        //TODO send left mouse down
        Log.d("EVENTCODE", 12 + "");
    }

    private void btnMouseLeftUp()
    {
        //TODO send left mouse up
        Log.d("EVENTCODE", 13 + "");
    }

    private void btnMouseRightDown()
    {
        //TODO send right mouse down
        Log.d("EVENTCODE", 14 + "");
    }

    private void btnMouseRightUp()
    {
        //TODO send right mouse up
        Log.d("EVENTCODE", 15 + "");
    }

    private void btnMouseUpDown()
    {
        //TODO send up mouse down
        Log.d("EVENTCODE", 16 + "");
    }

    private void btnMouseUpUp()
    {
        //TODO send up mouse up
        Log.d("EVENTCODE", 17 + "");
    }

    private void btnMouseDownDown()
    {
        //TODO send down mouse down
        Log.d("EVENTCODE", 18 + "");
    }

    private void btnMouseDownUp()
    {
        //TODO send down mouse up
        Log.d("EVENTCODE", 19 + "");
    }

    private void keyEnter()
    {
        //TODO send enter down, up
        Log.d("EVENTCODE", 20 + "");
    }

    private void keyBackspace(boolean down)
    {
        if(down)
        {
            //TODO send backspace down
            Log.d("EVENTCODE", 21 + "");
        }
        else
        {
            //TODO send backspace up
            Log.d("EVENTCODE", 22 + "");
        }
    }

    private void key(char key)
    {
        //TODO map key to linux integer and send
        Log.d("EVENTCODE", 23 + " '" + key + "'");
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
}
